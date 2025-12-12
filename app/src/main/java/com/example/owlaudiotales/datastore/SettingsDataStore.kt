package com.example.owlaudiotales.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.owlaudiotales.datastore.SettingsKeys.FIRST_LAUNCH
import com.example.owlaudiotales.ui.screens.settings.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsKeys {
    val CHILD_MODE = booleanPreferencesKey("child_mode")
    val AUTOPLAY = booleanPreferencesKey("autoplay_next")
    val MUSIC = booleanPreferencesKey("background_music")
    val TIMER = intPreferencesKey("sleep_timer")
    val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
}
class SettingsDataStore (private val context: Context){
    val settingsFlow: Flow<SettingsState> = context.settingsDataStore.data
        .map { prefs ->
            SettingsState(
                childMode = prefs[SettingsKeys.CHILD_MODE] ?: false,
                autoplayNext = prefs[SettingsKeys.AUTOPLAY] ?: false,
                backgroundMusic = prefs[SettingsKeys.MUSIC] ?: false,
                sleepTimerMinutes = prefs[SettingsKeys.TIMER] ?: 10

            )
        }
    val isFirstLaunchFlow: Flow<Boolean> = context.settingsDataStore.data
        .map { it[FIRST_LAUNCH] ?: true } // за замовчуванням true


    suspend fun saveChildMode(enable: Boolean) {
        context.settingsDataStore.edit { prefs -> prefs[SettingsKeys.CHILD_MODE] = enable }
    }

    suspend fun saveAutoPlay(enable: Boolean) {
        context.settingsDataStore.edit { prefs -> prefs[SettingsKeys.AUTOPLAY] = enable }
    }

    suspend fun saveMusicState(enable: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.MUSIC] = enable }
    }

    suspend fun saveTimer(minutes: Int) {
        context.settingsDataStore.edit { it[SettingsKeys.TIMER] = minutes }
    }

    suspend fun setFirstLaunchDone() {
        context.settingsDataStore.edit { it[FIRST_LAUNCH] = false }
    }

}