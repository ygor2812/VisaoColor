package com.visaocolor.services
import com.visaocolor.models.ColorBlindnessProfile
import com.visaocolor.models.ColorBlindnessType

class ChromaticFilterService {
    private val protanopiaMatrix = floatArrayOf(
        0.567f, 0.433f, 0.000f,
        0.558f, 0.442f, 0.000f,
        0.000f, 0.242f, 0.758f
    )

    private val deuteranopiaMatrix = floatArrayOf(
        0.625f, 0.375f, 0.000f,
        0.700f, 0.300f, 0.000f,
        0.000f, 0.300f, 0.700f
    )

    private val tritanopiaMatrix = floatArrayOf(
        0.950f, 0.050f, 0.000f,
        0.000f, 0.433f, 0.567f,
        0.000f, 0.475f, 0.525f
    )

    private val identityMatrix = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )

    fun getMatrixFor(type: ColorBlindnessType): FloatArray {
        return when (type) {
            ColorBlindnessType.PROTANOPIA -> protanopiaMatrix
            ColorBlindnessType.DEUTERANOPIA -> deuteranopiaMatrix
            ColorBlindnessType.TRITANOPIA -> tritanopiaMatrix
            ColorBlindnessType.NONE -> identityMatrix
        }
    }

    fun createProfile(type: ColorBlindnessType, active: Boolean = true): ColorBlindnessProfile {
        return ColorBlindnessProfile(
            name = type.displayName,
            type = type,
            filterMatrix = getMatrixFor(type),
            active = active
        )
    }

    fun applyFilter(r: Int, g: Int, b: Int, matrix: FloatArray): IntArray {
        val newR = (matrix[0] * r + matrix[1] * g + matrix[2] * b).toInt()
        val newG = (matrix[3] * r + matrix[4] * g + matrix[5] * b).toInt()
        val newB = (matrix[6] * r + matrix[7] * g + matrix[8] * b).toInt()

        return intArrayOf(
            newR.coerceIn(0, 255),
            newG.coerceIn(0, 255),
            newB.coerceIn(0, 255)
        )
    }
}