package com.visaocolor.services
class LightingMonitor {
    var minLuminance = 30
    var maxLuminance = 220
    enum class Status { TOO_DARK, OK, TOO_BRIGHT }
    //calcula luminancia media de uma lista de pixeis
    fun averageLuminance(pixels: List<IntArray>): Int {
        if (pixels.isEmpty()) return 0

        var sum = 0.0
        for (p in pixels) {
            sum += 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]
        }
        return (sum / pixels.size).toInt()
    }

    fun checkStatus(luminance: Int): Status {
        return when {
            luminance < minLuminance -> Status.TOO_DARK
            luminance > maxLuminance -> Status.TOO_BRIGHT
            else -> Status.OK
        }
    }

    fun updateThresholds(min: Int, max: Int) {
        if (min >= max) {
            throw IllegalArgumentException("min tem que ser menor que max")
        }
        minLuminance = min
        maxLuminance = max
    }
}
