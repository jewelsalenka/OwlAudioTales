package com.example.owlaudiotales.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDelete: (AudioItem) -> Unit,
    onEditClick: (AudioItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(audioList, key = { _, audio -> audio.id }) { index, audio ->
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
                    val offset = try { dismissState.requireOffset() } catch (e: IllegalStateException) { 0f }
                    val swipeDirection = when {
                        offset > 0f -> SwipeToDismissBoxValue.StartToEnd
                        offset < 0f -> SwipeToDismissBoxValue.EndToStart
                        else -> SwipeToDismissBoxValue.Settled
                    }
                    val color = when (swipeDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1976D2)
                        SwipeToDismissBoxValue.EndToStart -> Color(0xFFD32F2F)
                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = if (swipeDirection == SwipeToDismissBoxValue.StartToEnd)
                            Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        when (swipeDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_edit),
                                tint = Color.White
                            )
                            SwipeToDismissBoxValue.EndToStart -> Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = Color.White
                            )
                            else -> Unit
                        }
                    }
                }
            ) {
                AudioItemCard(
                    audio = audio,
                    isActive = activeId == audio.id,
                    isPlaying = isPlaying && activeId == audio.id,
                    canPlayNext = index < audioList.lastIndex,
                    canPlayPrevious = index > 0,
                    onClick = { onClick(audio.id) },
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious
                )
            }
        }
    }
}
