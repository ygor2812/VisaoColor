package com.visaocolor.models

enum class ColorBlindnessType(val displayName: String) {
    PROTANOPIA("Protanopia"),
    DEUTERANOPIA("Deuteranopia"),
    TRITANOPIA("Tritanopia"),
    NONE("Sem filtro");

    companion object {
        fun fromName(name: String): ColorBlindnessType {
            return values().firstOrNull { it.name == name } ?: NONE
        }
    }
}
data class ColorBlindnessProfile(
    val name: String,
    val type: ColorBlindnessType,
    val filterMatrix: FloatArray = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    ),
    val active: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorBlindnessProfile) return false
        return name == other.name
                && type == other.type
                && filterMatrix.contentEquals(other.filterMatrix)
                && active == other.active
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + filterMatrix.contentHashCode()
        result = 31 * result + active.hashCode()
        return result
    }
}
