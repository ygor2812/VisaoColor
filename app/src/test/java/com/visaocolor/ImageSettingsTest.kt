package com.visaocolor

import com.visaocolor.models.ImageSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImageSettingsTest {

    @Test
    fun valoresPadraoDevemEstarCorretos() {
        val s = ImageSettings()
        assertEquals(0, s.brilho)
        assertEquals(100, s.contraste)
        assertEquals(100, s.intensidade)
    }

    @Test
    fun brilhoMuitoAltoDeveDarErro() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageSettings(brilho = 200)
        }
    }

    @Test
    fun contrasteNegativoDeveDarErro() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageSettings(contraste = -10)
        }
    }
}
