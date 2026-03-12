package com.example.owlaudiotales.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    Column(Modifier.padding(16.dp)) {

        Text("Settings", fontSize = 22.sp)

        Spacer(Modifier.height(16.dp))

        SettingSwitch(
            title = "Child Mode",
            checked = state.childMode,
            onCheckedChange = { viewModel.childModeOn(it) }
        )

        SettingSwitch(
            title = "Autoplay next",
            checked = state.autoplayNext,
            onCheckedChange = { viewModel.autoPlayOn(it) }
        )

        SettingSwitch(
            title = "Background music",
            checked = state.backgroundMusic,
            onCheckedChange = { viewModel.turnOnBackgroundMusic(it) }
        )

        Spacer(Modifier.height(16.dp))
        Text("Sleep Timer: ${state.sleepTimerMinutes} minutes")

        Slider(
            value = state.sleepTimerMinutes.toFloat(),
            onValueChange = { viewModel.updateSleepTimer(it.toInt()) },
            valueRange = 0f..30f,
            steps = 5
        )
    }
}

@Composable
fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
