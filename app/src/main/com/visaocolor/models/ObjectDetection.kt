package com.visaocolor.models
import android.graphics.RectF

data class ObjectDetection(
    val objectName: String,
    val boundingBox: RectF,
    val confidence: Float
) {
    fun isLowConfidence(): Boolean {
        return confidence < 0.7f
    }
}
