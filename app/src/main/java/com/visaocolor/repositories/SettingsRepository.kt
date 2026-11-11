package com.visaocolor.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.first

// guarda as preferencias de acessibilidade do Modulo 5
// (o perfil de daltonismo fica no LocalStorageRepository)
class SettingsRepository(private val contexto: Context) {

    companion object {
        private val CHAVE_SENSIBILIDADE = intPreferencesKey("sensibilidade_alertas")
        private val CHAVE_VELOCIDADE = floatPreferencesKey("velocidade_voz")
        private val CHAVE_VOLUME = floatPreferencesKey("volume_voz")
    }

    suspend fun salvarSensibilidade(indice: Int) {
        contexto.appPrefs.edit { it[CHAVE_SENSIBILIDADE] = indice }
    }

    suspend fun obterSensibilidade(): Int {
        return contexto.appPrefs.data.first()[CHAVE_SENSIBILIDADE] ?: 1
    }

    suspend fun salvarVelocidadeVoz(valor: Float) {
        contexto.appPrefs.edit { it[CHAVE_VELOCIDADE] = valor }
    }

    suspend fun obterVelocidadeVoz(): Float {
        return contexto.appPrefs.data.first()[CHAVE_VELOCIDADE] ?: 1.0f
    }

    suspend fun salvarVolumeVoz(valor: Float) {
        contexto.appPrefs.edit { it[CHAVE_VOLUME] = valor }
    }

    suspend fun obterVolumeVoz(): Float {
        return contexto.appPrefs.data.first()[CHAVE_VOLUME] ?: 1.0f
    }
}
