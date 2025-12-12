package com.example.owlaudiotales.ui.screens.library

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.io.File

@Composable
fun AudioPlayerSection(
    audioPath: String,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(audioPath) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                mediaPlayer?.release()
                mediaPlayer = null
                isPlaying = false
                onPrevious()
            }) {
                Text("⏮")
            }

            Button(onClick = {
                if (mediaPlayer == null) {
                    val file = File(audioPath)
                    if (file.exists()) {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(file.absolutePath)
                            prepare()
                            start()
                        }
                        isPlaying = true
                    }
                } else {
                    mediaPlayer?.let {
                        if (isPlaying) {
                            it.pause()
                            isPlaying = false
                        } else {
                            it.start()
                            isPlaying = true
                        }
                    }
                }
            }) {
                Text(if (isPlaying) "⏸" else "▶")
            }

            Button(onClick = {
                mediaPlayer?.release()
                mediaPlayer = null
                isPlaying = false
                onNext()
            }) {
                Text("⏭")
            }
        }
    }
}
