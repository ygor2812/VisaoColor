package com.visaocolor.services

import android.graphics.Bitmap
import com.visaocolor.models.ColorBlindnessType
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

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
        val matRgba = Mat()
        Utils.bitmapToMat(origem, matRgba)

        val matRgb = Mat()
        Imgproc.cvtColor(matRgba, matRgb, Imgproc.COLOR_RGBA2RGB)

        val original = matRgb.clone()
        var processado = matRgb

        if (tipo != ColorBlindnessType.NONE) {
            val valores = filterService.obterMatrizPara(tipo)
            val matrizBrettel = Mat(3, 3, CvType.CV_32F)
            matrizBrettel.put(0, 0, floatArrayOf(
                valores[0], valores[1], valores[2],
                valores[3], valores[4], valores[5],
                valores[6], valores[7], valores[8]
            ))
            val filtrado = Mat()
            Core.transform(matRgb, filtrado, matrizBrettel)
            matrizBrettel.release()
            processado = filtrado
        }

        val comIntensidade = Mat()
        val alfa = intensidade / 100.0
        Core.addWeighted(processado, alfa, original, 1.0 - alfa, 0.0, comIntensidade)

        val fatorContraste = contraste / 100.0
        val deslocamentoBrilho = brilho * 2.55
        val ajustado = Mat()
        comIntensidade.convertTo(ajustado, -1, fatorContraste, deslocamentoBrilho)

        val matSaida = Mat()
        Imgproc.cvtColor(ajustado, matSaida, Imgproc.COLOR_RGB2RGBA)

        val bitmapSaida = Bitmap.createBitmap(origem.width, origem.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matSaida, bitmapSaida)

        matRgba.release()
        matRgb.release()
        original.release()
        if (processado !== matRgb) processado.release()
        comIntensidade.release()
        ajustado.release()
        matSaida.release()

        return bitmapSaida
    }
}