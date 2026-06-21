package com.visaocolor

import com.visaocolor.controllers.ColorIdentificationController
import com.visaocolor.repositories.SessionColorRepository
import com.visaocolor.services.ColorAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorIdentificationControllerTest {

    @Test
    fun identificarDeveRetornarCorESalvarNoHistorico() {
        val analisador = ColorAnalyzer()
        val historico = SessionColorRepository()
        val controller = ColorIdentificationController(analisador, historico)

        val resultado = controller.identificar(255, 0, 0)

        assertEquals("Vermelho", resultado.nome)
        assertEquals(1, controller.obterHistorico().size)
    }

    @Test
    fun multiplasIdentificacoesDevemAcumular() {
        val controller = ColorIdentificationController(
            ColorAnalyzer(),
            SessionColorRepository()
        )

        controller.identificar(255, 0, 0)
        controller.identificar(0, 255, 0)
        controller.identificar(0, 0, 255)

        assertEquals(3, controller.obterHistorico().size)
    }

    @Test
    fun limparHistoricoDeveEsvaziar() {
        val controller = ColorIdentificationController(
            ColorAnalyzer(),
            SessionColorRepository()
        )

        controller.identificar(255, 0, 0)
        controller.limparHistorico()

        assertEquals(0, controller.obterHistorico().size)
    }
}
