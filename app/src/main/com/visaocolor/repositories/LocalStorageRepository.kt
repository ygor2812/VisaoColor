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

class LocalStorageRepository(private val context: Context) {

    private val dataStore = context.appPrefs
    companion object {
        private val KEY_PROFILE = stringPreferencesKey("profile")
        private val KEY_FILTER_ON = booleanPreferencesKey("filter_on")
        private val KEY_BRIGHTNESS = intPreferencesKey("brightness")
        private val KEY_CONTRAST = intPreferencesKey("contrast")
        private val KEY_INTENSITY = intPreferencesKey("intensity")
        private val KEY_FIRST_RUN = booleanPreferencesKey("first_run")
        private val KEY_TERMS_OK = booleanPreferencesKey("terms_ok")
    }
//Perfil
    suspend fun saveProfile(profile: ColorBlindnessType) {
        dataStore.edit { it[KEY_PROFILE] = profile.name }
    }

    fun getProfile(): Flow<ColorBlindnessType> {
        return dataStore.data.map {
            val saved = it[KEY_PROFILE] ?: ColorBlindnessType.NONE.name
            ColorBlindnessType.fromName(saved)
        }
    }
    //Filtro ON/OFF
    suspend fun setFilterActive(active: Boolean) {
        dataStore.edit { it[KEY_FILTER_ON] = active }
    }
    suspend fun isFilterActive(): Boolean {
        return dataStore.data.map { it[KEY_FILTER_ON] ?: false }.first()
    }
    //Brilho, Intensidade e Contraste
    suspend fun saveBrightness(value: Int) {
        dataStore.edit { it[KEY_BRIGHTNESS] = value }
    }

    suspend fun saveContrast(value: Int) {
        dataStore.edit { it[KEY_CONTRAST] = value }
    }

    suspend fun saveIntensity(value: Int) {
        dataStore.edit { it[KEY_INTENSITY] = value }
    }

    suspend fun getImageSettings(): ImageSettings {
        return dataStore.data.map {
            ImageSettings(
                brightness = it[KEY_BRIGHTNESS] ?: 0,
                contrast = it[KEY_CONTRAST] ?: 100,
                intensity = it[KEY_INTENSITY] ?: 100
            )
        }.first()
    }
    //RESET Padrao de fabrica
    suspend fun resetImageSettings() {
        dataStore.edit {
            it[KEY_BRIGHTNESS] = 0
            it[KEY_CONTRAST] = 100
            it[KEY_INTENSITY] = 100
        }
    }

    suspend fun isFirstRun(): Boolean {
        return dataStore.data.map { it[KEY_FIRST_RUN] ?: true }.first()
    }

    suspend fun markAsOpened() {
        dataStore.edit { it[KEY_FIRST_RUN] = false }
    }
    //Termos
    suspend fun acceptTerms() {
        dataStore.edit { it[KEY_TERMS_OK] = true }
    }

    suspend fun hasAcceptedTerms(): Boolean {
        return dataStore.data.map { it[KEY_TERMS_OK] ?: false }.first()
    }
}
