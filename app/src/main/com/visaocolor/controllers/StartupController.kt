package com.visaocolor.controllers
import com.visaocolor.repositories.LocalStorageRepository

class StartupController(private val storage: LocalStorageRepository) {

    suspend fun isFirstAccess(): Boolean {
        return storage.isFirstRun()
    }

    suspend fun didAcceptTerms(): Boolean {
        return storage.hasAcceptedTerms()
    }

    suspend fun acceptTerms() {
        storage.acceptTerms()
    }

    suspend fun completeTutorial() {
        storage.markAsOpened()
    }
}
