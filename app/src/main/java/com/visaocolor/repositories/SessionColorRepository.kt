package com.visaocolor.repositories
import com.visaocolor.models.ColorRecord
//Historico de cores salva durante a sessao atual do app
class SessionColorRepository {
    private val colors = mutableListOf<ColorRecord>()
    private val maxSize = 100

    fun addColor(name: String, r: Int, g: Int, b: Int) {
        colors.add(ColorRecord(name, r, g, b))
        if (colors.size > maxSize) {
            val toRemove = colors.size - maxSize
            repeat(toRemove) { colors.removeAt(0) }
        }
    }

    fun getAll(): List<ColorRecord> = colors.toList()
    fun clear() {
        colors.clear()
    }
}
