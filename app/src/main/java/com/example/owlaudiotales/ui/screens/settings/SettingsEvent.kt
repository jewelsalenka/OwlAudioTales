package com.example.owlaudiotales.ui.screens.settings

sealed class SettingsEvent {
    data class ToggleChildMode(val enabled: Boolean) : SettingsEvent()
    data class ToggleAutoplay(val enabled: Boolean) : SettingsEvent()
    data class ToggleMusic(val enabled: Boolean) : SettingsEvent()
    data class ChangeSleepTimer(val minutes: Int) : SettingsEvent()
}