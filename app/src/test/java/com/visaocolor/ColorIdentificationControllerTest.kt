package com.visaocolor
import com.visaocolor.controllers.ColorIdentificationController
import com.visaocolor.repositories.SessionColorRepository
import com.visaocolor.services.ColorAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ColorIdentificationControllerTest {

    @Test
    fun identifyDeveConsultarAnalyzerESalvarNoHistorico() {
        val analyzer = mock<ColorAnalyzer>()
        whenever(analyzer.identifyColor(255, 0, 0)).thenReturn("Vermelho")

        val history = SessionColorRepository()
        val controller = ColorIdentificationController(analyzer, history)

        val resultado = controller.identify(255, 0, 0)

        assertEquals("Vermelho", resultado.name)
        assertEquals(1, controller.getHistory().size)
        verify(analyzer).identifyColor(255, 0, 0)
    }
}
