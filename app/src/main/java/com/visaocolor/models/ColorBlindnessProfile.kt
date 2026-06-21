package com.visaocolor.models

// tipos de daltonismo suportados pelo app
enum class ColorBlindnessType(val nomeExibicao: String) {
    PROTANOPIA("Protanopia"),
    DEUTERANOPIA("Deuteranopia"),
    TRITANOPIA("Tritanopia"),
    NONE("Sem filtro");

    companion object {
        fun apartirDoNome(nome: String): ColorBlindnessType {
            return values().firstOrNull { it.name == nome } ?: NONE
        }
    }
}

// guarda o perfil ativo do usuario (qual daltonismo + matriz)
data class ColorBlindnessProfile(
    val nome: String,
    val tipo: ColorBlindnessType,
    val matrizFiltro: FloatArray = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    ),
    val ativo: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorBlindnessProfile) return false
        return nome == other.nome
                && tipo == other.tipo
                && matrizFiltro.contentEquals(other.matrizFiltro)
                && ativo == other.ativo
    }

    override fun hashCode(): Int {
        var resultado = nome.hashCode()
        resultado = 31 * resultado + tipo.hashCode()
        resultado = 31 * resultado + matrizFiltro.contentHashCode()
        resultado = 31 * resultado + ativo.hashCode()
        return resultado
    }
}
