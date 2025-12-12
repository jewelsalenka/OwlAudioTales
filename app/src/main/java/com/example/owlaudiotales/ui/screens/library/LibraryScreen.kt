package com.example.owlaudiotales.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.model.AudioItem


@Composable
fun LibraryScreen(
    audioDao: AudioDao
) {
    val viewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(audioDao))

    val audioList = viewModel.audioList.collectAsStateWithLifecycle().value
    val activeId = viewModel.activeAudioId.collectAsStateWithLifecycle().value

    var showEditDialog by remember { mutableStateOf(false) }
    var editingAudio by remember { mutableStateOf<AudioItem?>(null) }


    LibraryContent(
        audioList = audioList,
        activeId = activeId,
        onClick = viewModel::onAudioClicked,
        onNext = viewModel::playNext,
        onPrevious = viewModel::playPrevious,
        onDelete = viewModel::deleteAudio,
        onEditClick = { audio ->
            editingAudio = audio
            showEditDialog = true }
    )



    if (showEditDialog && editingAudio != null) {
        EditMetadataDialog(
            audio = editingAudio!!,
            onDismiss = { showEditDialog = false },
            onSave = { title, author ->
                viewModel.updateAudioMetadata(editingAudio!!, title, author)
                showEditDialog = false
            },
            onTakePhoto = {
            }
        )
    }
}