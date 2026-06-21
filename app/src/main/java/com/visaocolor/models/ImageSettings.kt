package com.visaocolor.models

// configuracoes de brilho, contraste e intensidade do filtro
data class ImageSettings(
    val brilho: Int = 0,
    val contraste: Int = 100,
    val intensidade: Int = 100
) {
    init {
        if (brilho < -100 || brilho > 100) {
            throw IllegalArgumentException("brilho invalido: $brilho")
        }
        if (contraste < 0 || contraste > 200) {
            throw IllegalArgumentException("contraste invalido: $contraste")
        }
        if (intensidade < 0 || intensidade > 100) {
            throw IllegalArgumentException("intensidade invalida: $intensidade")
        }
    }

    companion object {
        val PADRAO = ImageSettings()
    }
}
