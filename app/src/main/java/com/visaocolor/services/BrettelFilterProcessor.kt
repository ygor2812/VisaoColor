package com.visaocolor.services

import android.graphics.Bitmap
import com.visaocolor.models.ColorBlindnessType
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

// Aplica a CORRECAO de cores (daltonizacao) usando OpenCV.
// Ao inves de so simular o daltonismo, calcula o que o daltonico "perde"
// e redistribui essas cores para os canais que ele ainda consegue enxergar,
// tornando as cores distinguiveis.
class BrettelFilterProcessor {

    companion object {
        init {
            System.loadLibrary("opencv_java4")
        }
    }

    private val filterService = ChromaticFilterService()

    fun aplicar(
        origem: Bitmap,
        tipo: ColorBlindnessType,
        brilho: Int = 0,
        contraste: Int = 100,
        intensidade: Int = 100
    ): Bitmap {
        // Bitmap -> Mat RGBA -> Mat RGB
        val matRgba = Mat()
        Utils.bitmapToMat(origem, matRgba)
        val matRgb = Mat()
        Imgproc.cvtColor(matRgba, matRgb, Imgproc.COLOR_RGBA2RGB)

        // converte para float para fazer as contas sem perder precisao
        val original = Mat()
        matRgb.convertTo(original, CvType.CV_32FC3)

        var corrigido = original.clone()

        if (tipo != ColorBlindnessType.NONE) {
            // 1. matriz de Brettel que SIMULA como o daltonico ve
            val valores = filterService.obterMatrizPara(tipo)
            val matrizSim = Mat(3, 3, CvType.CV_32F)
            matrizSim.put(0, 0, floatArrayOf(
                valores[0], valores[1], valores[2],
                valores[3], valores[4], valores[5],
                valores[6], valores[7], valores[8]
            ))

            // 2. gera a imagem simulada (como o daltonico enxergaria)
            val simulado = Mat()
            Core.transform(original, simulado, matrizSim)

            // 3. erro = o que se perde entre a imagem real e a simulada
            val erro = Mat()
            Core.subtract(original, simulado, erro)

            // 4. matriz que redistribui o erro para os canais visiveis
            //    - protan/deuter: joga o erro do vermelho no verde e no azul
            //    - tritan: joga o erro do azul no vermelho e no verde
            val matrizCorrecao = Mat(3, 3, CvType.CV_32F)
            if (tipo == ColorBlindnessType.TRITANOPIA) {
                matrizCorrecao.put(0, 0, floatArrayOf(
                    1f, 0f, 0.7f,
                    0f, 1f, 0.7f,
                    0f, 0f, 0f
                ))
            } else {
                matrizCorrecao.put(0, 0, floatArrayOf(
                    0f, 0f, 0f,
                    0.7f, 1f, 0f,
                    0.7f, 0f, 1f
                ))
            }

            // 5. aplica a redistribuicao e soma de volta na imagem original
            val desvio = Mat()
            Core.transform(erro, desvio, matrizCorrecao)
            Core.add(original, desvio, corrigido)

            matrizSim.release()
            simulado.release()
            erro.release()
            matrizCorrecao.release()
            desvio.release()
        }

        // intensidade: mistura entre a imagem original e a corrigida
        val alfa = intensidade / 100.0
        val saidaFloat = Mat()
        Core.addWeighted(corrigido, alfa, original, 1.0 - alfa, 0.0, saidaFloat)

        // brilho e contraste (convertTo ja satura entre 0 e 255)
        val fatorContraste = contraste / 100.0
        val deslocamentoBrilho = brilho * 2.55
        val ajustado = Mat()
        saidaFloat.convertTo(ajustado, CvType.CV_8UC3, fatorContraste, deslocamentoBrilho)

        // volta para RGBA e depois para Bitmap
        val matSaida = Mat()
        Imgproc.cvtColor(ajustado, matSaida, Imgproc.COLOR_RGB2RGBA)
        val bitmapSaida = Bitmap.createBitmap(origem.width, origem.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matSaida, bitmapSaida)

        matRgba.release()
        matRgb.release()
        original.release()
        corrigido.release()
        saidaFloat.release()
        ajustado.release()
        matSaida.release()

        return bitmapSaida
    }
}