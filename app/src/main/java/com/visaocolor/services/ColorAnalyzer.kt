package com.visaocolor.services

import kotlin.math.sqrt

// Identifica o nome da cor de um pixel RGB usando o modelo HSV
// (matiz = tom, saturacao = viveza, brilho = claridade).
// Versao simplificada: so cores basicas do dia a dia.
class ColorAnalyzer {

    fun identificarCor(r: Int, g: Int, b: Int): String {
        val rf = r / 255.0
        val gf = g / 255.0
        val bf = b / 255.0

        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min

        // H (matiz): o "tom" da cor, de 0 a 360 graus
        var h = 0.0
        if (delta != 0.0) {
            h = when (max) {
                rf -> 60 * ((((gf - bf) / delta) % 6 + 6) % 6)
                gf -> 60 * (((bf - rf) / delta) + 2)
                else -> 60 * (((rf - gf) / delta) + 4)
            }
        }
        if (h < 0) h += 360.0

        val s = if (max == 0.0) 0.0 else delta / max  // saturacao
        val v = max                                   // brilho

        // cores sem tom definido (preto, branco, cinza)
        if (v < 0.18) return "Preto"
        if (s < 0.22) {
            return if (v > 0.70) "Branco" else "Cinza"
        }
        // cor muito clara e pouco saturada = branco
        // (protege contra o branco "amarelado" causado pela camera)
        if (v > 0.88 && s < 0.40) return "Branco"

        // regiao quente: vermelho, laranja, amarelo e marrom
        val quente = h < 70 || h >= 345
        if (quente) {
            // proporcao do verde em relacao ao vermelho separa bem essas cores
            val razaoVerde = if (r == 0) 0.0 else g.toDouble() / r.toDouble()

            if (v < 0.55 && s > 0.4 && razaoVerde < 0.8) return "Marrom"  // quente e escuro
            if (v > 0.75 && s < 0.45 && razaoVerde < 0.85) return "Rosa"  // quente e claro

            return when {
                razaoVerde < 0.4 -> "Vermelho"
                razaoVerde < 0.78 -> "Laranja"
                else -> "Amarelo"
            }
        }

        // demais cores separadas pela matiz
        return when {
            h < 170 -> "Verde"
            h < 260 -> "Azul"
            h < 320 -> "Roxo"
            else -> "Rosa"
        }
    }

    // Distancia entre duas cores no espaco RGB (usada pelos testes).
    fun distancia(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return sqrt(dr * dr + dg * dg + db * db)
    }
}