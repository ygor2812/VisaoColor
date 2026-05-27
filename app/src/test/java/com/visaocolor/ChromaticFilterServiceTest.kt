package com.visaocolor

import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.services.ChromaticFilterService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChromaticFilterServiceTest {

    private val service = ChromaticFilterService()

    @Test
    fun matrizDeveTer9Valores() {
        val matrix = service.getMatrixFor(ColorBlindnessType.DEUTERANOPIA)
        assertEquals(9, matrix.size)
    }

    @Test
    fun matrizNoneDeveSerIdentidade() {
        val matrix = service.getMatrixFor(ColorBlindnessType.NONE)
        assertEquals(1f, matrix[0], 0.001f)
        assertEquals(1f, matrix[4], 0.001f)
        assertEquals(1f, matrix[8], 0.001f)
    }

    @Test
    fun aplicarMatrizIdentidadeDeveManterPixel() {
        val identity = service.getMatrixFor(ColorBlindnessType.NONE)
        val resultado = service.applyFilter(100, 150, 200, identity)
        assertEquals(100, resultado[0])
        assertEquals(150, resultado[1])
        assertEquals(200, resultado[2])
    }

    @Test
    fun valoresDevemFicarEntre0e255() {
        val matrix = service.getMatrixFor(ColorBlindnessType.DEUTERANOPIA)
        val resultado = service.applyFilter(255, 255, 255, matrix)
        for (valor in resultado) {
            assertTrue(valor in 0..255)
        }
    }
}
