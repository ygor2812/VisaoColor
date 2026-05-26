package com.visaocolor.controllers

import com.visaocolor.models.ColorRecord
import com.visaocolor.repositories.SessionColorRepository
import com.visaocolor.services.ColorAnalyzer

class ColorIdentificationController(
    private val analyzer: ColorAnalyzer,
    private val history: SessionColorRepository
) {
    fun identify(r: Int, g: Int, b: Int): ColorRecord {
        val name = analyzer.identifyColor(r, g, b)
        history.addColor(name, r, g, b)
        return ColorRecord(name, r, g, b)
    }

    fun getHistory(): List<ColorRecord> = history.getAll()

    fun clearHistory() = history.clear()
}
