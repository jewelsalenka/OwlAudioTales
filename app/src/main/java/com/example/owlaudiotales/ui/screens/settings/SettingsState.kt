package com.example.owlaudiotales.ui.screens.settings

data class SettingsState(
    val childMode: Boolean = false,
    val autoplayNext: Boolean = false,
    val backgroundMusic: Boolean = true,
    val sleepTimerMinutes: Int = 10
)