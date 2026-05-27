package com.visaocolor.controllers
import com.visaocolor.models.ColorBlindnessProfile
import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.repositories.LocalStorageRepository
import com.visaocolor.services.ChromaticFilterService
import kotlinx.coroutines.flow.first

class ColorFilterController(
    private val filterService: ChromaticFilterService,
    private val storage: LocalStorageRepository
) {
    private var currentProfile: ColorBlindnessProfile? = null

    suspend fun changeProfile(type: ColorBlindnessType): ColorBlindnessProfile {
        val profile = filterService.createProfile(type, active = true)
        currentProfile = profile
        storage.saveProfile(type)
        storage.setFilterActive(true)
        return profile
    }

    suspend fun turnOff() {
        currentProfile = currentProfile?.copy(active = false)
        storage.setFilterActive(false)
    }

    suspend fun turnOn() {
        currentProfile = currentProfile?.copy(active = true)
        storage.setFilterActive(true)
    }

    suspend fun loadLast(): ColorBlindnessProfile {
        val savedType = storage.getProfile().first()
        val active = storage.isFilterActive()
        return filterService.createProfile(savedType, active).also {
            currentProfile = it
        }
    }

    fun getCurrent() = currentProfile
}
