package com.example.owlaudiotales.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.owlaudiotales.datastore.SettingsKeys.FIRST_LAUNCH
import com.example.owlaudiotales.model.BackgroundTrack
import com.example.owlaudiotales.ui.screens.settings.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsKeys {
    val CHILD_MODE = booleanPreferencesKey("child_mode")
    val AUTOPLAY = booleanPreferencesKey("autoplay_next")
    val MUSIC = booleanPreferencesKey("background_music")
    val MUSIC_TRACK = stringPreferencesKey("background_music_track")
    val MUSIC_VOLUME = intPreferencesKey("background_music_volume")
    val TIMER = intPreferencesKey("sleep_timer")
    val TIMER_SECONDS = intPreferencesKey("sleep_timer_seconds")
    val TIMER_ENABLED = booleanPreferencesKey("sleep_timer_enabled")
    val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
}
class SettingsDataStore (private val context: Context){
    val settingsFlow: Flow<SettingsState> = context.settingsDataStore.data
        .map { prefs ->
            SettingsState(
                childMode = prefs[SettingsKeys.CHILD_MODE] ?: false,
                autoplayNext = prefs[SettingsKeys.AUTOPLAY] ?: false,
                backgroundMusic = prefs[SettingsKeys.MUSIC] ?: false,
                backgroundTrack = BackgroundTrack.entries.firstOrNull {
                    it.id == prefs[SettingsKeys.MUSIC_TRACK]
                } ?: BackgroundTrack.DEFAULT,
                backgroundVolume = prefs[SettingsKeys.MUSIC_VOLUME]?.coerceIn(0, 100)
                    ?: SettingsState.DEFAULT_BACKGROUND_VOLUME,
                sleepTimerSeconds = prefs[SettingsKeys.TIMER_SECONDS]
                    ?: (prefs[SettingsKeys.TIMER]?.times(60) ?: 0),
                sleepTimerEnabled = prefs[SettingsKeys.TIMER_ENABLED] ?: false

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

    suspend fun saveMusicTrack(track: BackgroundTrack) {
        context.settingsDataStore.edit { it[SettingsKeys.MUSIC_TRACK] = track.id }
    }

    suspend fun saveMusicVolume(percent: Int) {
        context.settingsDataStore.edit { it[SettingsKeys.MUSIC_VOLUME] = percent.coerceIn(0, 100) }
    }

    suspend fun saveTimer(seconds: Int) {
        context.settingsDataStore.edit { it[SettingsKeys.TIMER_SECONDS] = seconds }
    }

    suspend fun saveTimerEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.TIMER_ENABLED] = enabled }
    }

    suspend fun setFirstLaunchDone() {
        context.settingsDataStore.edit { it[FIRST_LAUNCH] = false }
    }

}
