package com.visaocolor.services

import com.visaocolor.models.ColorBlindnessProfile
import com.visaocolor.models.ColorBlindnessType

// aplica o filtro de Brettel pra ajudar pessoas com daltonismo
class ChromaticFilterService {
    private val matrizProtanopia = floatArrayOf(
        0.567f, 0.433f, 0.000f,
        0.558f, 0.442f, 0.000f,
        0.000f, 0.242f, 0.758f
    )
    private val matrizDeuteranopia = floatArrayOf(
        0.625f, 0.375f, 0.000f,
        0.700f, 0.300f, 0.000f,
        0.000f, 0.300f, 0.700f
    )
    private val matrizTritanopia = floatArrayOf(
        0.950f, 0.050f, 0.000f,
        0.000f, 0.433f, 0.567f,
        0.000f, 0.475f, 0.525f
    )
    private val matrizIdentidade = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )

    fun obterMatrizPara(tipo: ColorBlindnessType): FloatArray {
        return when (tipo) {
            ColorBlindnessType.PROTANOPIA -> matrizProtanopia
            ColorBlindnessType.DEUTERANOPIA -> matrizDeuteranopia
            ColorBlindnessType.TRITANOPIA -> matrizTritanopia
            ColorBlindnessType.NONE -> matrizIdentidade
        }
    }

    fun criarPerfil(tipo: ColorBlindnessType, ativo: Boolean = true): ColorBlindnessProfile {
        return ColorBlindnessProfile(
            nome = tipo.nomeExibicao,
            tipo = tipo,
            matrizFiltro = obterMatrizPara(tipo),
            ativo = ativo
        )
    }

    // aplica a matriz num pixel RGB
    fun aplicarFiltro(r: Int, g: Int, b: Int, matriz: FloatArray): IntArray {
        val novoR = (matriz[0] * r + matriz[1] * g + matriz[2] * b).toInt()
        val novoG = (matriz[3] * r + matriz[4] * g + matriz[5] * b).toInt()
        val novoB = (matriz[6] * r + matriz[7] * g + matriz[8] * b).toInt()

        return intArrayOf(
            novoR.coerceIn(0, 255),
            novoG.coerceIn(0, 255),
            novoB.coerceIn(0, 255)
        )
    }
}
