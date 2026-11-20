package com.example.owlaudiotales.ui.screens.settings

data class SettingsState(
    val childMode: Boolean = false,
    val autoplayNext: Boolean = false,
    val backgroundMusic: Boolean = true,
    val sleepTimerSeconds: Int = 0,
    val sleepTimerEnabled: Boolean = false
)
