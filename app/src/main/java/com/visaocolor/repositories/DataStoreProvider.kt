package com.visaocolor.repositories
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.appPrefs: DataStore<Preferences> by preferencesDataStore(name = "visao_prefs")