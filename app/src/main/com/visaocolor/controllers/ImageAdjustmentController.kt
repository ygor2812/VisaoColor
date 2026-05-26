package com.visaocolor.controllers

import com.visaocolor.models.ImageSettings
import com.visaocolor.repositories.LocalStorageRepository

class ImageAdjustmentController(private val storage: LocalStorageRepository) {

    private var current = ImageSettings()

    suspend fun setBrightness(value: Int) {
        current = current.copy(brightness = value)
        storage.saveBrightness(value)
    }

    suspend fun setContrast(value: Int) {
        current = current.copy(contrast = value)
        storage.saveContrast(value)
    }

    suspend fun setIntensity(value: Int) {
        current = current.copy(intensity = value)
        storage.saveIntensity(value)
    }

    suspend fun resetAll() {
        current = ImageSettings()
        storage.resetImageSettings()
    }

    suspend fun loadSaved(): ImageSettings {
        current = storage.getImageSettings()
        return current
    }

    fun get() = current
}
