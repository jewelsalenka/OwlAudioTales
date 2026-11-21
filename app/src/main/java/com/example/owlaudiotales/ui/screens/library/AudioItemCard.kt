package com.example.owlaudiotales.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.owlaudiotales.R
import com.example.owlaudiotales.model.AudioItem

@Composable
fun AudioItemCard(
    audio: AudioItem,
    isActive: Boolean,
    isPlaying: Boolean,
    isRepeating: Boolean,
    canPlayNext: Boolean,
    canPlayPrevious: Boolean,
    onClick: () -> Unit,
    onPlayPause: () -> Unit,
    onRepeatChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = audio.coverImagePath,
                    contentDescription = audio.title,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.placeholder_image),
                    placeholder = painterResource(id = R.drawable.placeholder_image)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = audio.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.author_format, audio.author),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.duration_format, audio.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isActive) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    AudioPlayerSection(
                        isPlaying = isPlaying,
                        isRepeating = isRepeating,
                        canPlayNext = canPlayNext,
                        canPlayPrevious = canPlayPrevious,
                        onPlayPause = onPlayPause,
                        onRepeatChange = onRepeatChange,
                        onNext = onNext,
                        onPrevious = onPrevious
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioItemCardPreview() {
    AudioItemCard(
        audio = AudioItem(
            title = "Казка про Котика",
            author = "Іванка Франко",
            filePath = "",
            duration = 300,
            createdAt = 0L
        ),
        isActive = true,
        isPlaying = false,
        isRepeating = false,
        canPlayNext = true,
        canPlayPrevious = false,
        onClick = {},
        onPlayPause = {},
        onRepeatChange = {},
        onNext = {},
        onPrevious = {}
    )
}
