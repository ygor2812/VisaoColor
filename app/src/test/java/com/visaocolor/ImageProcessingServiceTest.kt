package com.visaocolor

import com.visaocolor.services.ImageProcessingService
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageProcessingServiceTest {

    private val servico = ImageProcessingService()

    @Test
    fun brilhoZeroNaoDeveAlterarPixel() {
        val resultado = servico.ajustarBrilho(intArrayOf(100, 150, 200), 0)
        assertEquals(100, resultado[0])
        assertEquals(150, resultado[1])
        assertEquals(200, resultado[2])
    }

    @Test
    fun brilhoMaximoDeveSaturarEm255() {
        val resultado = servico.ajustarBrilho(intArrayOf(200, 200, 200), 100)
        for (v in resultado) {
            assertEquals(255, v)
        }
    }

    @Test
    fun misturarComIntensidadeZeroDeveRetornarOriginal() {
        val resultado = servico.misturar(
            intArrayOf(100, 100, 100),
            intArrayOf(200, 200, 200),
            0
        )
        assertEquals(100, resultado[0])
    }

    @Test
    fun misturarComIntensidade100DeveRetornarFiltrado() {
        val resultado = servico.misturar(
            intArrayOf(100, 100, 100),
            intArrayOf(200, 200, 200),
            100
        )
        assertEquals(200, resultado[0])
    }
}
