package com.visaocolor

import com.visaocolor.services.ImageProcessingService
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageProcessingServiceTest {

    private val service = ImageProcessingService()

    @Test
    fun brilhoZeroNaoDeveAlterarPixel() {
        val resultado = service.adjustBrightness(intArrayOf(100, 150, 200), 0)
        assertEquals(100, resultado[0])
        assertEquals(150, resultado[1])
        assertEquals(200, resultado[2])
    }

    @Test
    fun brilhoMaximoDeveSaturarEm255() {
        val resultado = service.adjustBrightness(intArrayOf(200, 200, 200), 100)
        for (v in resultado) {
            assertEquals(255, v)
        }
    }

    @Test
    fun blendComIntensidadeZeroDeveRetornarOriginal() {
        val resultado = service.blend(
            intArrayOf(100, 100, 100),
            intArrayOf(200, 200, 200),
            0
        )
        assertEquals(100, resultado[0])
    }

    @Test
    fun blendComIntensidade100DeveRetornarFiltrado() {
        val resultado = service.blend(
            intArrayOf(100, 100, 100),
            intArrayOf(200, 200, 200),
            100
        )
        assertEquals(200, resultado[0])
    }
}
