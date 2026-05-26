package com.visaocolor.services
import com.visaocolor.models.ObjectDetection
class ObjectDetectionEngine {
    var modelName = "ssd_mobilenet_v1.tflite"
    var minConfidence = 0.5f
    private var paused = false
    //ainda a ser implementado, aqui vai ser carregado o modelo tflite(TensorFlow)
    fun loadModel() {
        // val tfliteModel = FileUtil.loadMappedFile(context, modelName)
        // interpreter = Interpreter(tfliteModel)
    }

    //A fazer: Rodar inferencia no frame
    fun detect(frame: Any): List<ObjectDetection> {
        if (paused) return emptyList()
        return emptyList()
    }

    fun filterByConfidence(detections: List<ObjectDetection>): List<ObjectDetection> {
        return detections.filter { it.confidence >= minConfidence }
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    fun isPaused() = paused
}
