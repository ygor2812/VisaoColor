package com.visaocolor

import com.visaocolor.services.ColorAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorAnalyzerTest {

    private val analyzer = ColorAnalyzer()

    @Test
    fun deveIdentificarVermelhoPuro() {
        val nome = analyzer.identifyColor(255, 0, 0)
        assertEquals("Vermelho", nome)
    }

    @Test
    fun deveIdentificarVerdePuro() {
        val nome = analyzer.identifyColor(0, 255, 0)
        assertEquals("Verde", nome)
    }

    @Test
    fun deveIdentificarAzulPuro() {
        val nome = analyzer.identifyColor(0, 0, 255)
        assertEquals("Azul", nome)
    }

    @Test
    fun deveIdentificarPreto() {
        val nome = analyzer.identifyColor(0, 0, 0)
        assertEquals("Preto", nome)
    }

    @Test
    fun distanciaParaCoresIguaisDeveSerZero() {
        val d = analyzer.distance(100, 100, 100, 100, 100, 100)
        assertEquals(0.0, d, 0.001)
    }

    @Test
    fun distanciaEntreCoresDiferentesDeveSerPositiva() {
        val d = analyzer.distance(0, 0, 0, 255, 255, 255)
        assertTrue(d > 0)
    }
}
