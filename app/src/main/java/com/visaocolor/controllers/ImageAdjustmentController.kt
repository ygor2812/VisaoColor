package com.visaocolor.controllers
import com.visaocolor.models.ImageSettings
import com.visaocolor.repositories.LocalStorageRepository

class ImageAdjustmentController(private val armazenamento: LocalStorageRepository) {

    private var atual = ImageSettings()

    suspend fun definirBrilho(valor: Int) {
        atual = atual.copy(brilho = valor)
        armazenamento.salvarBrilho(valor)
    }

    suspend fun definirContraste(valor: Int) {
        atual = atual.copy(contraste = valor)
        armazenamento.salvarContraste(valor)
    }

    suspend fun definirIntensidade(valor: Int) {
        atual = atual.copy(intensidade = valor)
        armazenamento.salvarIntensidade(valor)
    }

    suspend fun restaurarTudo() {
        atual = ImageSettings()
        armazenamento.restaurarConfiguracoesImagem()
    }

    suspend fun carregarSalvas(): ImageSettings {
        atual = armazenamento.obterConfiguracoesImagem()
        return atual
    }

    fun obter() = atual
}
