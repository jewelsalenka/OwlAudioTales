package com.example.owlaudiotales.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.owlaudiotales.model.AudioItem

@Composable
fun LibraryScreen() {
    val viewModel: LibraryViewModel = hiltViewModel()

    val audioList = viewModel.audioList.collectAsStateWithLifecycle().value
    val activeId = viewModel.activeAudioId.collectAsStateWithLifecycle().value
    val isPlaying = viewModel.isPlaying.collectAsStateWithLifecycle().value
    val isRepeating = viewModel.isRepeating.collectAsStateWithLifecycle().value

    var showEditDialog by remember { mutableStateOf(false) }
    var editingAudio by remember { mutableStateOf<AudioItem?>(null) }

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
        onEditClick = { audio ->
            editingAudio = audio
            showEditDialog = true
        }
    )

    if (showEditDialog && editingAudio != null) {
        EditMetadataDialog(
            audio = editingAudio!!,
            onDismiss = { showEditDialog = false },
            onSave = { title, author ->
                viewModel.updateAudioMetadata(editingAudio!!, title, author)
                showEditDialog = false
            },
            onTakePhoto = {}
        )
    }
}
