package com.example.owlaudiotales.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlaudiotales.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(val settingsDataStore: SettingsDataStore) : ViewModel() {

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

    fun updateSleepTimer(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.saveTimer(minutes)
        }
    }

}