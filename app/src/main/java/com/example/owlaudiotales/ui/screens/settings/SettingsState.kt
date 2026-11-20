package com.example.owlaudiotales.ui.screens.settings

import com.example.owlaudiotales.model.BackgroundTrack

data class SettingsState(
    val childMode: Boolean = false,
    val autoplayNext: Boolean = false,
    val backgroundMusic: Boolean = false,
    val backgroundTrack: BackgroundTrack = BackgroundTrack.DEFAULT,
    val backgroundVolume: Int = DEFAULT_BACKGROUND_VOLUME,
    val sleepTimerSeconds: Int = 0,
    val sleepTimerEnabled: Boolean = false
) {
    companion object {
        const val DEFAULT_BACKGROUND_VOLUME = 12
    }
}
