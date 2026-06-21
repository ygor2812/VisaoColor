package com.visaocolor.services

// ajusta brilho, contraste e mistura o filtro com a imagem original
class ImageProcessingService {

    // aumenta ou diminui o brilho de um pixel
    fun ajustarBrilho(rgb: IntArray, brilho: Int): IntArray {
        val delta = (brilho * 2.55).toInt()
        return intArrayOf(
            (rgb[0] + delta).coerceIn(0, 255),
            (rgb[1] + delta).coerceIn(0, 255),
            (rgb[2] + delta).coerceIn(0, 255)
        )
    }

    // aumenta ou diminui o contraste
    fun ajustarContraste(rgb: IntArray, contraste: Int): IntArray {
        val fator = contraste / 100f
        return intArrayOf(
            aplicarFatorContraste(rgb[0], fator),
            aplicarFatorContraste(rgb[1], fator),
            aplicarFatorContraste(rgb[2], fator)
        )
    }

    private fun aplicarFatorContraste(valor: Int, fator: Float): Int {
        val centralizado = valor - 128
        val resultado = (centralizado * fator + 128).toInt()
        return resultado.coerceIn(0, 255)
    }

    // mistura proporcionalmente a imagem original com a filtrada
    // intensidade=0 mostra original, intensidade=100 mostra filtro puro
    fun misturar(original: IntArray, filtrado: IntArray, intensidade: Int): IntArray {
        val alfa = intensidade / 100f
        val resultado = IntArray(3)
        for (i in 0..2) {
            resultado[i] = (original[i] * (1 - alfa) + filtrado[i] * alfa).toInt()
            resultado[i] = resultado[i].coerceIn(0, 255)
        }
        return resultado
    }
}
