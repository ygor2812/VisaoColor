package com.visaocolor.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.models.ImageSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// salva e recupera as configuracoes do app no DataStore
class LocalStorageRepository(private val contexto: Context) {

    private val dataStore = contexto.appPrefs

    // chaves usadas pra salvar cada configuracao
    companion object {
        private val CHAVE_PERFIL = stringPreferencesKey("perfil")
        private val CHAVE_FILTRO_ATIVO = booleanPreferencesKey("filtro_ativo")
        private val CHAVE_BRILHO = intPreferencesKey("brilho")
        private val CHAVE_CONTRASTE = intPreferencesKey("contraste")
        private val CHAVE_INTENSIDADE = intPreferencesKey("intensidade")
        private val CHAVE_PRIMEIRO_ACESSO = booleanPreferencesKey("primeiro_acesso")
        private val CHAVE_TERMOS_OK = booleanPreferencesKey("termos_ok")
    }

    //Perfil
    suspend fun salvarPerfil(perfil: ColorBlindnessType) {
        dataStore.edit { it[CHAVE_PERFIL] = perfil.name }
    }

    fun obterPerfil(): Flow<ColorBlindnessType> {
        return dataStore.data.map {
            val salvo = it[CHAVE_PERFIL] ?: ColorBlindnessType.NONE.name
            ColorBlindnessType.apartirDoNome(salvo)
        }
    }

    //Filtro ligado/desligado
    suspend fun definirFiltroAtivo(ativo: Boolean) {
        dataStore.edit { it[CHAVE_FILTRO_ATIVO] = ativo }
    }

    suspend fun filtroEstaAtivo(): Boolean {
        return dataStore.data.map { it[CHAVE_FILTRO_ATIVO] ?: false }.first()
    }

    //Brilho, contraste, intensidade
    suspend fun salvarBrilho(valor: Int) {
        dataStore.edit { it[CHAVE_BRILHO] = valor }
    }

    suspend fun salvarContraste(valor: Int) {
        dataStore.edit { it[CHAVE_CONTRASTE] = valor }
    }

    suspend fun salvarIntensidade(valor: Int) {
        dataStore.edit { it[CHAVE_INTENSIDADE] = valor }
    }

    suspend fun obterConfiguracoesImagem(): ImageSettings {
        return dataStore.data.map {
            ImageSettings(
                brilho = it[CHAVE_BRILHO] ?: 0,
                contraste = it[CHAVE_CONTRASTE] ?: 100,
                intensidade = it[CHAVE_INTENSIDADE] ?: 100
            )
        }.first()
    }

    // restaura tudo pro padrao
    suspend fun restaurarConfiguracoesImagem() {
        dataStore.edit {
            it[CHAVE_BRILHO] = 0
            it[CHAVE_CONTRASTE] = 100
            it[CHAVE_INTENSIDADE] = 100
        }
    }

    //Primeiro acesso
    suspend fun ehPrimeiroAcesso(): Boolean {
        return dataStore.data.map { it[CHAVE_PRIMEIRO_ACESSO] ?: true }.first()
    }

    suspend fun marcarComoAberto() {
        dataStore.edit { it[CHAVE_PRIMEIRO_ACESSO] = false }
    }

    //Aceite dos termos
    suspend fun aceitarTermos() {
        dataStore.edit { it[CHAVE_TERMOS_OK] = true }
    }

    suspend fun aceitouTermos(): Boolean {
        return dataStore.data.map { it[CHAVE_TERMOS_OK] ?: false }.first()
    }
}
