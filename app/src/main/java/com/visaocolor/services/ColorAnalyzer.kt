package com.visaocolor.services

import kotlin.math.sqrt

// Identifica o nome da cor de um pixel RGB.
// Converte a cor para o modelo HSV (matiz, saturacao, brilho),
// porque nesse modelo e mais facil separar cores parecidas como laranja e marrom.
class ColorAnalyzer {

    fun identificarCor(r: Int, g: Int, b: Int): String {
        // converte os canais de 0..255 para 0..1
        val rf = r / 255.0
        val gf = g / 255.0
        val bf = b / 255.0
        // maior e menor canal servem para calcular saturacao e brilho
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min
        // H (matiz) = o "tom" da cor, em graus de 0 a 360
        // (0=vermelho, 120=verde, 240=azul)
        var h = 0.0
        if (delta != 0.0) {
            h = when (max) {
                rf -> 60 * ((((gf - bf) / delta) % 6 + 6) % 6)
                gf -> 60 * (((bf - rf) / delta) + 2)
                else -> 60 * (((rf - gf) / delta) + 4)
            }
        }
        if (h < 0) h += 360.0
        // S (saturacao) (0 = cinza, 1 = cor pura)
        val s = if (max == 0.0) 0.0 else delta / max
        // V (brilho) (0 = preto, 1 = clara)
        val v = max
        // brilho muito baixo = preto, independente da cor
        if (v < 0.18) return "Preto"
        if (s < 0.12) {             // saturacao muito baixa = tons de cinza
            return when {
                v > 0.85 -> "Branco"
                v > 0.35 -> "Cinza"
                else -> "Cinza Escuro"
            }
        }
        // regiao "quente": vermelho, laranja, amarelo e marrom
        // (matiz perto do 0 ou acima de 345)
        val quente = h < 70 || h >= 345
        // proporcao do verde em relacao ao vermelho.
        if (quente) {
            val razaoVerde = if (r == 0) 0.0 else g.toDouble() / r.toDouble()

            if (v < 0.55 && s > 0.4 && razaoVerde < 0.8) return "Marrom"  // cor quente porem escura e viva = marrom

            if (v > 0.75 && s < 0.45 && razaoVerde < 0.85) return "Rosa"  // cor quente clara e pouco saturada = rosa

            return when {
                razaoVerde < 0.4 -> "Vermelho"
                razaoVerde < 0.78 -> "Laranja"
                else -> "Amarelo"
            }
        }
        // demais cores separadas apenas pela matiz
        return when {
            h < 160 -> "Verde"
            h < 195 -> "Ciano"
            h < 255 -> "Azul"
            h < 290 -> "Roxo"
            else -> if (s < 0.5) "Rosa" else "Magenta"
        }
    }
    // Distancia entre duas cores no espaco RGB (formula euclidiana).
    fun distancia(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return sqrt(dr * dr + dg * dg + db * db)
    }
}