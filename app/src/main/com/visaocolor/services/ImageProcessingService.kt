package com.visaocolor.services
//Aumento de Brilho
class ImageProcessingService {
    fun adjustBrightness(rgb: IntArray, brightness: Int): IntArray {
        // converte de -100..100 pra -255..255
        val delta = (brightness * 2.55).toInt()
        return intArrayOf(
            (rgb[0] + delta).coerceIn(0, 255),
            (rgb[1] + delta).coerceIn(0, 255),
            (rgb[2] + delta).coerceIn(0, 255)
        )
    }
    //Contraste -/+
    fun adjustContrast(rgb: IntArray, contrast: Int): IntArray {
        val factor = contrast / 100f
        return intArrayOf(
            applyContrastFactor(rgb[0], factor),
            applyContrastFactor(rgb[1], factor),
            applyContrastFactor(rgb[2], factor)
        )
    }

    private fun applyContrastFactor(value: Int, factor: Float): Int {
        val centered = value - 128
        val result = (centered * factor + 128).toInt()
        return result.coerceIn(0, 255)
    }
    //Mix proporcional da imagem original com a imagem com filtro
    fun blend(original: IntArray, filtered: IntArray, intensity: Int): IntArray {
        val alpha = intensity / 100f
        val result = IntArray(3)
        for (i in 0..2) {
            result[i] = (original[i] * (1 - alpha) + filtered[i] * alpha).toInt()
            result[i] = result[i].coerceIn(0, 255)
        }
        return result
    }
}
