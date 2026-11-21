package com.example.owlaudiotales.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.owlaudiotales.R

@Preview(showBackground = true)
@Composable
private fun AudioPlayerSectionPreview() {
    AudioPlayerSection(
        isPlaying = false,
        isRepeating = false,
        canPlayNext = true,
        canPlayPrevious = false,
        onPlayPause = {},
        onRepeatChange = {},
        onNext = {},
        onPrevious = {}
    )
}

@Composable
fun AudioPlayerSection(
    isPlaying: Boolean,
    isRepeating: Boolean,
    canPlayNext: Boolean,
    canPlayPrevious: Boolean,
    onPlayPause: () -> Unit,
    onRepeatChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
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

        FilterChip(
            selected = isRepeating,
            onClick = { onRepeatChange(!isRepeating) },
            label = { Text(stringResource(R.string.player_loop)) },
            modifier = Modifier.widthIn(min = 80.dp),
            leadingIcon = if (isRepeating) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}
