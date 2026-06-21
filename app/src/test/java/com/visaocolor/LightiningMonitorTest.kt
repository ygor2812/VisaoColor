package com.visaocolor

import com.visaocolor.services.LightingMonitor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LightingMonitorTest {

    private val monitor = LightingMonitor()

    @Test
    fun ambienteEscuroDeveRetornarEscuro() {
        val status = monitor.verificarStatus(10)
        assertEquals(LightingMonitor.Status.ESCURO, status)
    }

    @Test
    fun ambienteNormalDeveRetornarOk() {
        val status = monitor.verificarStatus(128)
        assertEquals(LightingMonitor.Status.OK, status)
    }

    @Test
    fun ambienteMuitoIluminadoDeveRetornarClaro() {
        val status = monitor.verificarStatus(250)
        assertEquals(LightingMonitor.Status.CLARO, status)
    }

    @Test
    fun luminanciaPretaDeveSerZero() {
        val pixels = listOf(intArrayOf(0, 0, 0))
        assertEquals(0, monitor.luminanciaMedia(pixels))
    }

    @Test
    fun atualizarLimitesComMinimoMaiorDeveDarErro() {
        assertThrows(IllegalArgumentException::class.java) {
            monitor.atualizarLimites(200, 50)
        }
    }
}
