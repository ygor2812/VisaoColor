package com.visaocolor.models

import android.graphics.RectF

// um objeto detectado pela IA
data class ObjectDetection(
    val nomeObjeto: String,
    val caixaDelimitadora: RectF,
    val confianca: Float
) {
    fun confiancaBaixa(): Boolean {
        return confianca < 0.7f
    }
}
