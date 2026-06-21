package com.visaocolor.controllers
import com.visaocolor.repositories.LocalStorageRepository

class StartupController(private val armazenamento: LocalStorageRepository) {

    suspend fun ehPrimeiroAcesso(): Boolean {
        return armazenamento.ehPrimeiroAcesso()
    }

    suspend fun aceitouTermos(): Boolean {
        return armazenamento.aceitouTermos()
    }

    suspend fun aceitarTermos() {
        armazenamento.aceitarTermos()
    }

    suspend fun concluirTutorial() {
        armazenamento.marcarComoAberto()
    }
}
