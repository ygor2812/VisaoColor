package com.visaocolor.services

// Mede a luminosidade media da cena e classifica a condicao de iluminacao
class LightingMonitor {

    enum class Status { ESCURO, ADEQUADO, CLARO }

    // Tres niveis de sensibilidade: quanto maior, mais cedo o alerta aparece
    enum class Sensibilidade(val rotulo: String, val minima: Double, val maxima: Double) {
        BAIXA("Baixa", 18.0, 240.0),
        MEDIA("Media", 30.0, 220.0),
        ALTA("Alta", 45.0, 200.0)
    }

    var sensibilidade: Sensibilidade = Sensibilidade.MEDIA

    // Luminancia conforme a recomendacao ITU-R BT.601
    fun luminanciaMedia(pixels: List<IntArray>): Double {
        if (pixels.isEmpty()) return 0.0
        var soma = 0.0
        for (p in pixels) {
            soma += 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]
        }
        return soma / pixels.size
    }

    fun verificarStatus(luminancia: Double): Status {
        return when {
            luminancia < sensibilidade.minima -> Status.ESCURO
            luminancia > sensibilidade.maxima -> Status.CLARO
            else -> Status.ADEQUADO
        }
    }
}