package com.visaocolor.controllers
import com.visaocolor.models.ColorBlindnessProfile
import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.repositories.LocalStorageRepository
import com.visaocolor.services.ChromaticFilterService
import kotlinx.coroutines.flow.first

class ColorFilterController(
    private val servicoFiltro: ChromaticFilterService,
    private val armazenamento: LocalStorageRepository
) {
    private var perfilAtual: ColorBlindnessProfile? = null

    suspend fun trocarPerfil(tipo: ColorBlindnessType): ColorBlindnessProfile {
        val perfil = servicoFiltro.criarPerfil(tipo, ativo = true)
        perfilAtual = perfil
        armazenamento.salvarPerfil(tipo)
        armazenamento.definirFiltroAtivo(true)
        return perfil
    }

    suspend fun desligar() {
        perfilAtual = perfilAtual?.copy(ativo = false)
        armazenamento.definirFiltroAtivo(false)
    }

    suspend fun ligar() {
        perfilAtual = perfilAtual?.copy(ativo = true)
        armazenamento.definirFiltroAtivo(true)
    }

    suspend fun carregarUltimo(): ColorBlindnessProfile {
        val tipoSalvo = armazenamento.obterPerfil().first()
        val ativo = armazenamento.filtroEstaAtivo()
        return servicoFiltro.criarPerfil(tipoSalvo, ativo).also {
            perfilAtual = it
        }
    }

    fun obterAtual() = perfilAtual
}
