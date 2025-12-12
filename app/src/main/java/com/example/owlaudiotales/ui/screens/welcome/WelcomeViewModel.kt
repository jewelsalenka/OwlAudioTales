package com.example.owlaudiotales.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlaudiotales.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(private val settingsDataStore: SettingsDataStore) : ViewModel() {

    private val _startDestination = MutableStateFlow<String>("")
    val startDestination: StateFlow<String> = _startDestination

    init {
        viewModelScope.launch {
            settingsDataStore.isFirstLaunchFlow.collect { isFirst ->
                _startDestination.value = if (isFirst) "welcome" else "library"
            }
        }
    }

    fun markFirstLaunchComplete() {
        viewModelScope.launch {
            settingsDataStore.setFirstLaunchDone()
        }
    }
}