package com.example.owlaudiotales.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.owlaudiotales.R
import com.example.owlaudiotales.model.AudioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    audioList: List<AudioItem>,
    activeId: Long?,
    isPlaying: Boolean,
    onClick: (Long) -> Unit,
    onPlayPause: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDelete: (AudioItem) -> Unit,
    onEditClick: (AudioItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(audioList, key = { it.id }) { audio ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    when (value) {
                        SwipeToDismissBoxValue.EndToStart -> {
                            onDelete(audio)
                            true
                        }
                        SwipeToDismissBoxValue.StartToEnd -> {
                            onEditClick(audio)
                            false
                        }
                        else -> false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = true,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    val isEditAction = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isEditAction) Color(0xFF1976D2) else Color(0xFFD32F2F))
                            .padding(horizontal = 20.dp),
                        contentAlignment = if (isEditAction) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = if (isEditAction) Icons.Default.Edit else Icons.Default.Delete,
                            contentDescription = stringResource(if (isEditAction) R.string.action_edit else R.string.action_delete),
                            tint = Color.White
                        )
                    }
                }
            ) {
                AudioItemCard(
                    audio = audio,
                    isActive = activeId == audio.id,
                    isPlaying = isPlaying && activeId == audio.id,
                    onClick = { onClick(audio.id) },
                    onPlayPause = { onPlayPause(audio.filePath) },
                    onNext = { onNext() },
                    onPrevious = { onPrevious() }
                )
            }
        }
    }
}
