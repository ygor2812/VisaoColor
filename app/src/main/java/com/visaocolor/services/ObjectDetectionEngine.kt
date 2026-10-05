package com.visaocolor.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.visaocolor.models.ObjectDetection
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

// Motor de reconhecimento de objetos usando TensorFlow Lite.
// Carrega o modelo MobileNet SSD e o mapa de classes (COCO) dos assets.
class ObjectDetectionEngine(private val contexto: Context) {

    private var interpretador: Interpreter? = null
    private val rotulos = mutableListOf<String>()

    // o MobileNet SSD espera imagem 300x300 e devolve ate 10 objetos por frame
    private val tamanhoEntrada = 300
    private val maxDeteccoes = 10

    // so aceita deteccoes com pelo menos 60% de confianca
    var confiancaMinima = 0.65f

    // carrega o modelo e os rotulos uma unica vez
    fun carregar() {
        if (interpretador != null) return
        val modelo = carregarArquivoModelo("ssd_mobilenet_v1.tflite")
        interpretador = Interpreter(modelo)
        carregarRotulos("labelmap.txt")
    }

    // le o arquivo .tflite dos assets e mapeia na memoria
    private fun carregarArquivoModelo(nome: String): ByteBuffer {
        val fd = contexto.assets.openFd(nome)
        val entrada = FileInputStream(fd.fileDescriptor)
        val canal = entrada.channel
        return canal.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    // le o labelmap.txt (uma classe por linha)
    private fun carregarRotulos(nome: String) {
        rotulos.clear()
        contexto.assets.open(nome).bufferedReader().forEachLine { rotulos.add(it.trim()) }
    }

    // roda a deteccao num frame e devolve a lista de objetos encontrados
    fun detectar(origem: Bitmap): List<ObjectDetection> {
        val interp = interpretador ?: return emptyList()

        // 1. redimensiona a imagem para 300x300 e converte para o formato do modelo
        val redimensionado = Bitmap.createScaledBitmap(origem, tamanhoEntrada, tamanhoEntrada, true)
        val entrada = converterParaBuffer(redimensionado)

        // 2. prepara os 4 vetores de saida do modelo
        val locais = Array(1) { Array(maxDeteccoes) { FloatArray(4) } }   // caixas
        val classes = Array(1) { FloatArray(maxDeteccoes) }               // indices das classes
        val notas = Array(1) { FloatArray(maxDeteccoes) }                 // confianca
        val quantidade = FloatArray(1)                                    // quantos objetos

        val saidas = HashMap<Int, Any>()
        saidas[0] = locais
        saidas[1] = classes
        saidas[2] = notas
        saidas[3] = quantidade

        // 3. executa a inferencia
        interp.runForMultipleInputsOutputs(arrayOf(entrada), saidas)

        // 4. monta a lista filtrando pela confianca minima
        val resultado = mutableListOf<ObjectDetection>()
        for (i in 0 until maxDeteccoes) {
            val confianca = notas[0][i]
            if (confianca < confiancaMinima) continue

            val indice = classes[0][i].toInt()
            // offset +1: a primeira linha do labelmap e "???", entao o indice
            // do modelo (0 = person) precisa pular uma posicao
            val nome = if (indice + 1 in rotulos.indices) rotulos[indice + 1] else "objeto"

            // as coordenadas vem normalizadas (0..1); multiplica pelo tamanho real
            val caixa = RectF(
                locais[0][i][1] * origem.width,
                locais[0][i][0] * origem.height,
                locais[0][i][3] * origem.width,
                locais[0][i][2] * origem.height
            )
            resultado.add(ObjectDetection(nome, caixa, confianca))
        }

        // 5. ordena pelos mais confiaveis e devolve so os 3 melhores
        return resultado.sortedByDescending { it.confianca }.take(3)
    }

    // converte o bitmap para um buffer de bytes (formato uint8 que o modelo espera)
    private fun converterParaBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(tamanhoEntrada * tamanhoEntrada * 3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(tamanhoEntrada * tamanhoEntrada)
        bitmap.getPixels(pixels, 0, tamanhoEntrada, 0, 0, tamanhoEntrada, tamanhoEntrada)
        for (p in pixels) {
            buffer.put(((p shr 16) and 0xFF).toByte()) // R
            buffer.put(((p shr 8) and 0xFF).toByte())  // G
            buffer.put((p and 0xFF).toByte())          // B
        }
        return buffer
    }
}