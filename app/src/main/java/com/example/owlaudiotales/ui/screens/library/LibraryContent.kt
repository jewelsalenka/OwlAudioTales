package com.example.owlaudiotales.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.owlaudiotales.model.AudioItem

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryContent(
    audioList: List<AudioItem>,
    activeId: Long?,
    onClick: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDelete: (AudioItem) -> Unit,
    onEditClick: (AudioItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
            items(audioList.size) { index ->
                val audio = audioList[index]
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        when (it) {
                            DismissValue.DismissedToStart -> {
                                onDelete(audio)
                                true
                            }

                            DismissValue.DismissedToEnd -> {
                                onEditClick(audio)
                                false
                            }

                            else -> false
                        }
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(
                        DismissDirection.StartToEnd,
                        DismissDirection.EndToStart
                    ),
                    background = {
                        val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                        val color = when (direction) {
                            DismissDirection.StartToEnd -> Color(0xFF1976D2) // Edit
                            DismissDirection.EndToStart -> Color(0xFFD32F2F) // Delete
                        }
                        val icon = when (direction) {
                            DismissDirection.StartToEnd -> Icons.Default.Edit
                            DismissDirection.EndToStart -> Icons.Default.Delete
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = when (direction) {
                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                DismissDirection.EndToStart -> Alignment.CenterEnd
                            }
                        ) {
                            Icon(icon, contentDescription = null, tint = Color.White)
                        }
                    },
                    dismissContent = {
                        AudioItemCard(
                            audio = audio,
                            isActive = activeId == audio.id,
                            onClick = { onClick(audio.id) },
                            onNext = { onNext() },
                            { onPrevious() }
                        )
                    }
                )
            }
        }
    }
}
