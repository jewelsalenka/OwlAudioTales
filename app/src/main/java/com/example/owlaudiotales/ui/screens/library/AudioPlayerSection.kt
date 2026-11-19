package com.example.owlaudiotales.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.owlaudiotales.R

@Preview(showBackground = true)
@Composable
private fun AudioPlayerSectionPreview() {
    AudioPlayerSection(
        isPlaying = false,
        canPlayNext = true,
        canPlayPrevious = false,
        onPlayPause = {},
        onNext = {},
        onPrevious = {}
    )
}

@Composable
fun AudioPlayerSection(
    isPlaying: Boolean,
    canPlayNext: Boolean,
    canPlayPrevious: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onPrevious, enabled = canPlayPrevious) {
                Text(stringResource(R.string.player_previous))
            }

            Button(onClick = onPlayPause) {
                Text(stringResource(if (isPlaying) R.string.player_pause else R.string.player_play))
            }

            Button(onClick = onNext, enabled = canPlayNext) {
                Text(stringResource(R.string.player_next))
            }
        }
    }
}
