package com.example.owlaudiotales.ui.screens.settings

import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.owlaudiotales.R
import com.example.owlaudiotales.model.BackgroundTrack
import com.example.owlaudiotales.service.AudioPlaybackService
import com.example.owlaudiotales.service.BackgroundPreviewCommand
import com.example.owlaudiotales.ui.components.SecondaryScreenTopBar
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SecondaryScreenTopBar(stringResource(R.string.settings_title), onBack) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingSwitch(
                title = stringResource(R.string.setting_autoplay_next),
                checked = state.autoplayNext,
                onCheckedChange = { viewModel.autoPlayOn(it) }
            )

            SettingSwitch(
                title = stringResource(R.string.setting_background_music),
                checked = state.backgroundMusic,
                onCheckedChange = viewModel::turnOnBackgroundMusic
            )

            if (state.backgroundMusic) {
                BackgroundMusicControls(
                    track = state.backgroundTrack,
                    volume = state.backgroundVolume,
                    onTrackSelected = viewModel::selectBackgroundTrack,
                    onVolumeChanged = viewModel::setBackgroundVolume
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingSwitch(
                title = stringResource(R.string.setting_sleep_timer),
                checked = state.sleepTimerEnabled,
                onCheckedChange = viewModel::setSleepTimerEnabled
            )

            if (state.sleepTimerEnabled) {
                SleepTimerPicker(
                    savedSeconds = state.sleepTimerSeconds,
                    onDurationChanged = viewModel::updateSleepTimer
                )
            }
        }
    }
}

@Composable
private fun BackgroundMusicControls(
    track: BackgroundTrack,
    volume: Int,
    onTrackSelected: (BackgroundTrack) -> Unit,
    onVolumeChanged: (Int) -> Unit
) {
    var sliderVolume by remember(volume) { mutableStateOf(volume.toFloat()) }
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var pendingTrack by remember { mutableStateOf<BackgroundTrack?>(null) }

    DisposableEffect(context) {
        val token = SessionToken(context, ComponentName(context, AudioPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        var disposed = false
        future.addListener({
            if (!disposed) {
                controller = runCatching { future.get() }.getOrNull()
                pendingTrack?.let { controller?.previewBackground(it) }
                pendingTrack = null
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed = true
            controller?.previewBackground(null)
            MediaController.releaseFuture(future)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        pendingTrack = null
        controller?.previewBackground(null)
    }

    Column(Modifier.selectableGroup()) {
        BackgroundTrack.entries.forEach { option ->
            val selected = option == track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = {
                            onTrackSelected(option)
                            val connectedController = controller
                            if (connectedController == null) {
                                pendingTrack = option
                            } else {
                                connectedController.previewBackground(option)
                            }
                        }
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected, onClick = null)
                Text(stringResource(option.title), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.background_volume_format, sliderVolume.roundToInt()),
        style = MaterialTheme.typography.bodyMedium
    )
    Slider(
        value = sliderVolume,
        onValueChange = { sliderVolume = it },
        onValueChangeFinished = { onVolumeChanged(sliderVolume.roundToInt()) },
        valueRange = 0f..100f
    )
}

private fun MediaController.previewBackground(track: BackgroundTrack?) {
    val args = Bundle()
    track?.let { args.putString(BackgroundPreviewCommand.TRACK_ID, it.id) }
    sendCustomCommand(BackgroundPreviewCommand.command, args)
}

@Composable
private fun SleepTimerPicker(savedSeconds: Int, onDurationChanged: (Int) -> Unit) {
    var duration by remember { mutableStateOf(savedSeconds.coerceIn(0, 3 * 3600)) }
    val hours = duration / 3600
    val minutes = duration / 60 % 60
    val seconds = duration % 60
    val maxRemainder = if (hours == 3) 0 else 59

    fun setDuration(hours: Int, minutes: Int, seconds: Int) {
        duration = (hours * 3600 + minutes * 60 + seconds).coerceAtMost(3 * 3600)
        onDurationChanged(duration)
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        TimerWheel(stringResource(R.string.timer_hours), hours, 3) {
            setDuration(it, minutes, seconds)
        }
        TimerWheel(stringResource(R.string.timer_minutes), minutes, maxRemainder) {
            setDuration(hours, it, seconds)
        }
        TimerWheel(stringResource(R.string.timer_seconds), seconds, maxRemainder) {
            setDuration(hours, minutes, it)
        }
    }
}

@Composable
private fun TimerWheel(label: String, value: Int, maximum: Int, onValueChanged: (Int) -> Unit) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        AndroidView(
            modifier = Modifier.size(width = 72.dp, height = 120.dp),
            factory = { context ->
                NumberPicker(context).apply {
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                }
            },
            update = { picker ->
                picker.contentDescription = label
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    picker.setTextColor(textColor)
                }
                picker.maxValue = maximum
                picker.wrapSelectorWheel = false
                picker.value = value
                picker.isEnabled = maximum > 0
                picker.setOnValueChangedListener { _, _, newValue -> onValueChanged(newValue) }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingSwitch(
    title: String = stringResource(R.string.setting_autoplay_next),
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
