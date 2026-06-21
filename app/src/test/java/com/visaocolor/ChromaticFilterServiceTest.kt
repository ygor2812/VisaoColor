package com.visaocolor

import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.services.ChromaticFilterService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChromaticFilterServiceTest {

    private val servico = ChromaticFilterService()

    @Test
    fun matrizDeveTer9Valores() {
        val matriz = servico.obterMatrizPara(ColorBlindnessType.DEUTERANOPIA)
        assertEquals(9, matriz.size)
    }

    @Test
    fun matrizNoneDeveSerIdentidade() {
        val matriz = servico.obterMatrizPara(ColorBlindnessType.NONE)
        assertEquals(1f, matriz[0], 0.001f)
        assertEquals(1f, matriz[4], 0.001f)
        assertEquals(1f, matriz[8], 0.001f)
    }

    @Test
    fun aplicarMatrizIdentidadeDeveManterPixel() {
        val identidade = servico.obterMatrizPara(ColorBlindnessType.NONE)
        val resultado = servico.aplicarFiltro(100, 150, 200, identidade)
        assertEquals(100, resultado[0])
        assertEquals(150, resultado[1])
        assertEquals(200, resultado[2])
    }

    @Test
    fun valoresDevemFicarEntre0e255() {
        val matriz = servico.obterMatrizPara(ColorBlindnessType.DEUTERANOPIA)
        val resultado = servico.aplicarFiltro(255, 255, 255, matriz)
        for (valor in resultado) {
            assertTrue(valor in 0..255)
        }
    }
}
