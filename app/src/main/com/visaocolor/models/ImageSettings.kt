package com.visaocolor.models
data class ImageSettings(
    val brightness: Int = 0,      // -100 a +100
    val contrast: Int = 100,      // 0 a 200 (100 = sem alteração)
    val intensity: Int = 100      // 0 a 100 (porcentagem do filtro)
) {
    init {
        if (brightness < -100 || brightness > 100) {
            throw IllegalArgumentException("brilho inválido: $brightness")
        }
        if (contrast < 0 || contrast > 200) {
            throw IllegalArgumentException("contraste inválido: $contrast")
        }
        if (intensity < 0 || intensity > 100) {
            throw IllegalArgumentException("intensidade inválida: $intensity")
        }
    }

    companion object {
        val DEFAULT = ImageSettings()
    }
}
