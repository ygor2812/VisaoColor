package com.visaocolor.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
data class ColorRecord(
    val name: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun toHex(): String {
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
