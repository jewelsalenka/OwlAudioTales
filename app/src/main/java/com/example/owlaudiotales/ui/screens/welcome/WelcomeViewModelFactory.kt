package com.example.owlaudiotales.ui.screens.welcome


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.owlaudiotales.datastore.SettingsDataStore

class WelcomeViewModelFactory(
    private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WelcomeViewModel(settingsDataStore) as T
    }
}