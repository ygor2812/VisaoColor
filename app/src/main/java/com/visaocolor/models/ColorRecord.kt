package com.visaocolor.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// uma cor identificada quando o usuario toca na tela
data class ColorRecord(
    val nome: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val horario: Long = System.currentTimeMillis()
) {
    fun horarioFormatado(): String {
        val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formato.format(Date(horario))
    }

    fun paraHex(): String {
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
