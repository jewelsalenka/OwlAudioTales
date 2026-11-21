package com.example.owlaudiotales.ui.screens.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.owlaudiotales.R
import com.example.owlaudiotales.model.AudioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpenSettings: () -> Unit, onCreateRecording: () -> Unit) {
    val viewModel: LibraryViewModel = hiltViewModel()

    val audioList = viewModel.audioList.collectAsStateWithLifecycle().value
    val activeId = viewModel.activeAudioId.collectAsStateWithLifecycle().value
    val isPlaying = viewModel.isPlaying.collectAsStateWithLifecycle().value
    val isRepeating = viewModel.isRepeating.collectAsStateWithLifecycle().value
    val canCreateRecording = viewModel.canCreateRecording.collectAsStateWithLifecycle().value

    var editingAudio by remember { mutableStateOf<AudioItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, stringResource(R.string.settings_title))
                    }
                }
            )
        },
        floatingActionButton = {
            if (canCreateRecording) {
                val label = stringResource(R.string.create_recording)
                ExtendedFloatingActionButton(
                    onClick = {
                        if (viewModel.canCreateRecording.value) onCreateRecording()
                    },
                    icon = { Icon(Icons.Default.Mic, contentDescription = label) },
                    text = { Text(label) }
                )
            }
        }
    ) { padding ->
        LibraryContent(
            audioList = audioList,
            activeId = activeId,
            isPlaying = isPlaying,
            isRepeating = isRepeating,
            onClick = viewModel::onAudioClicked,
            onPlayPause = viewModel::onPlayPauseClicked,
            onRepeatChange = viewModel::setRepeatEnabled,
            onNext = viewModel::playNext,
            onPrevious = viewModel::playPrevious,
            onDelete = viewModel::deleteAudio,
            onEditClick = { editingAudio = it },
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = if (canCreateRecording) 88.dp else 0.dp)
        )
    }

    editingAudio?.let { audio ->
        EditMetadataDialog(
            audio = audio,
            onDismiss = { editingAudio = null },
            onSave = { title, author ->
                viewModel.updateAudioMetadata(audio, title, author)
                editingAudio = null
            },
            onTakePhoto = {}
        )
    }
}
