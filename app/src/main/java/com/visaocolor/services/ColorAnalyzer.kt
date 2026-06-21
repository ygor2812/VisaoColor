package com.visaocolor.services

import kotlin.math.sqrt

// identifica o nome da cor mais proxima de um pixel RGB
// usa distancia euclidiana - simples mas funciona pra cores basicas
class ColorAnalyzer {

    // banco de cores em portugues
    private val mapaCores = mapOf(
        "Vermelho" to Triple(255, 0, 0),
        "Verde" to Triple(0, 255, 0),
        "Azul" to Triple(0, 0, 255),
        "Amarelo" to Triple(255, 255, 0),
        "Ciano" to Triple(0, 255, 255),
        "Magenta" to Triple(255, 0, 255),
        "Branco" to Triple(255, 255, 255),
        "Preto" to Triple(0, 0, 0),
        "Cinza" to Triple(128, 128, 128),
        "Laranja" to Triple(255, 165, 0),
        "Rosa" to Triple(255, 192, 203),
        "Roxo" to Triple(128, 0, 128),
        "Marrom" to Triple(139, 69, 19),
        "Verde Musgo" to Triple(85, 107, 47),
        "Azul Marinho" to Triple(0, 0, 128),
        "Bege" to Triple(245, 245, 220)
    )

    fun identificarCor(r: Int, g: Int, b: Int): String {
        var nomeMaisProximo = "Desconhecido"
        var menorDistancia = Double.MAX_VALUE

        for ((nome, rgb) in mapaCores) {
            val dist = distancia(r, g, b, rgb.first, rgb.second, rgb.third)
            if (dist < menorDistancia) {
                menorDistancia = dist
                nomeMaisProximo = nome
            }
        }

        return nomeMaisProximo
    }

    // calcula a distancia euclidiana entre duas cores no espaco RGB
    fun distancia(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return sqrt(dr * dr + dg * dg + db * db)
    }
}
