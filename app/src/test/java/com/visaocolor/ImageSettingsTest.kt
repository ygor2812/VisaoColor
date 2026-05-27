package com.visaocolor
import com.visaocolor.models.ImageSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImageSettingsTest {

    @Test
    fun valoresPadraoDevemEstarCorretos() {
        val s = ImageSettings()
        assertEquals(0, s.brightness)
        assertEquals(100, s.contrast)
        assertEquals(100, s.intensity)
    }

    @Test
    fun brilhoMuitoAltoDeveDarErro() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageSettings(brightness = 200)
        }
    }

    @Test
    fun contrasteNegativoDeveDarErro() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageSettings(contrast = -10)
        }
    }
}
