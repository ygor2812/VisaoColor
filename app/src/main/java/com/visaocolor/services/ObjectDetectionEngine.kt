package com.visaocolor.services

import com.visaocolor.models.ObjectDetection

// motor da deteccao de objetos com TensorFlow Lite
// vai ser usado o modelo MobileNet SSD treinado no dataset COCO
class ObjectDetectionEngine {

    var nomeModelo = "ssd_mobilenet_v1.tflite"
    var confiancaMinima = 0.5f
    private var pausado = false

    // A fazer no Modulo 4: carregar o modelo TFLite aqui
    fun carregarModelo() {
    }

    // A fazer no Modulo 4: rodar inferencia no frame
    fun detectar(frame: Any): List<ObjectDetection> {
        if (pausado) return emptyList()
        return emptyList()
    }

    fun filtrarPorConfianca(deteccoes: List<ObjectDetection>): List<ObjectDetection> {
        return deteccoes.filter { it.confianca >= confiancaMinima }
    }

    fun pausar() {
        pausado = true
    }

    fun retomar() {
        pausado = false
    }

    fun estaPausado() = pausado
}
