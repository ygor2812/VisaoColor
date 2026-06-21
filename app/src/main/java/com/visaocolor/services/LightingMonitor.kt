package com.visaocolor.services

// detecta se o ambiente esta escuro demais ou claro demais
// pra avisar o usuario que a leitura pode estar incorreta
class LightingMonitor {

    var luminanciaMinima = 30
    var luminanciaMaxima = 220

    enum class Status { ESCURO, OK, CLARO }

    // calcula luminancia media de uma lista de pixels
    // formula padrao: Y = 0.299R + 0.587G + 0.114B
    fun luminanciaMedia(pixels: List<IntArray>): Int {
        if (pixels.isEmpty()) return 0

        var soma = 0.0
        for (p in pixels) {
            soma += 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]
        }
        return (soma / pixels.size).toInt()
    }

    fun verificarStatus(luminancia: Int): Status {
        return when {
            luminancia < luminanciaMinima -> Status.ESCURO
            luminancia > luminanciaMaxima -> Status.CLARO
            else -> Status.OK
        }
    }

    fun atualizarLimites(minimo: Int, maximo: Int) {
        if (minimo >= maximo) {
            throw IllegalArgumentException("minimo tem que ser menor que maximo")
        }
        luminanciaMinima = minimo
        luminanciaMaxima = maximo
    }
}
