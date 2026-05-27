package com.visaocolor
import com.visaocolor.services.LightingMonitor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LightingMonitorTest {

    private val monitor = LightingMonitor()

    @Test
    fun ambienteEscuroDeveRetornarTooDark() {
        val status = monitor.checkStatus(10)
        assertEquals(LightingMonitor.Status.TOO_DARK, status)
    }

    @Test
    fun ambienteNormalDeveRetornarOk() {
        val status = monitor.checkStatus(128)
        assertEquals(LightingMonitor.Status.OK, status)
    }

    @Test
    fun ambienteMuitoIluminadoDeveRetornarTooBright() {
        val status = monitor.checkStatus(250)
        assertEquals(LightingMonitor.Status.TOO_BRIGHT, status)
    }

    @Test
    fun luminanciaPretaDeveSerZero() {
        val pixels = listOf(intArrayOf(0, 0, 0))
        assertEquals(0, monitor.averageLuminance(pixels))
    }

    @Test
    fun atualizarThresholdsComMinMaiorDeveDarErro() {
        assertThrows(IllegalArgumentException::class.java) {
            monitor.updateThresholds(200, 50)
        }
    }
}
