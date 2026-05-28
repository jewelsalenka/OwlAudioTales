package com.example.owlaudiotales.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.owlaudiotales.R

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.padding(16.dp)) {

        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        SettingSwitch(
            title = stringResource(R.string.setting_child_mode),
            checked = state.childMode,
            onCheckedChange = { viewModel.childModeOn(it) }
        )

        SettingSwitch(
            title = stringResource(R.string.setting_autoplay_next),
            checked = state.autoplayNext,
            onCheckedChange = { viewModel.autoPlayOn(it) }
        )

        SettingSwitch(
            title = stringResource(R.string.setting_background_music),
            checked = state.backgroundMusic,
            onCheckedChange = { viewModel.turnOnBackgroundMusic(it) }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.sleep_timer_format, state.sleepTimerMinutes),
            style = MaterialTheme.typography.bodyMedium
        )

        var sliderValue by remember(state.sleepTimerMinutes) {
            mutableStateOf(state.sleepTimerMinutes.toFloat())
        }

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { viewModel.updateSleepTimer(sliderValue.toInt()) },
            valueRange = 0f..30f,
            steps = 5
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingSwitch(
    title: String = "Child Mode",
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
