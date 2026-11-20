package com.example.owlaudiotales.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlaudiotales.datastore.SettingsDataStore
import com.example.owlaudiotales.model.BackgroundTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch { settingsDataStore.settingsFlow.collect { _state.value = it } }
    }

    fun childModeOn(isChecked: Boolean) {
        viewModelScope.launch { settingsDataStore.saveChildMode(isChecked) }
    }

    fun autoPlayOn(isChecked: Boolean) {
        viewModelScope.launch { settingsDataStore.saveAutoPlay(isChecked) }
    }

    fun turnOnBackgroundMusic(isChecked: Boolean) {
        viewModelScope.launch { settingsDataStore.saveMusicState(isChecked)}
    }

    fun selectBackgroundTrack(track: BackgroundTrack) {
        viewModelScope.launch { settingsDataStore.saveMusicTrack(track) }
    }

    fun setBackgroundVolume(percent: Int) {
        viewModelScope.launch { settingsDataStore.saveMusicVolume(percent) }
    }

    fun setSleepTimerEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.saveTimerEnabled(enabled) }
    }

    fun updateSleepTimer(seconds: Int) {
        viewModelScope.launch {
            settingsDataStore.saveTimer(seconds)
        }
    }

}
