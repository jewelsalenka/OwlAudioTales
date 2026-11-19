package com.example.owlaudiotales.ui.screens.library

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.model.AudioItem
import com.example.owlaudiotales.service.AudioPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioDao: AudioDao
) : ViewModel() {

    val audioList: StateFlow<List<AudioItem>> = audioDao.getAllAudio()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeAudioId = MutableStateFlow<Long?>(null)
    val activeAudioId: StateFlow<Long?> = _activeAudioId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK ||
                reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
            ) {
                _activeAudioId.value = mediaItem?.mediaId?.toLongOrNull()
            }
        }
    }

    init {
        connectToService()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, AudioPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)
            observeAudioList()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun observeAudioList() {
        viewModelScope.launch {
            audioList.collect { items -> updatePlaylist(items) }
        }
    }

    private fun updatePlaylist(items: List<AudioItem>) {
        val controller = mediaController ?: return
        if (items.isEmpty()) {
            controller.clearMediaItems()
            return
        }

        val mediaItems = items.map { it.toMediaItem() }
        val currentItemId = controller.currentMediaItem?.mediaId
        val currentIndex = items.indexOfFirst { it.id.toString() == currentItemId }
        val startIndex = if (currentIndex >= 0) currentIndex else 0
        val startPosition = if (currentIndex >= 0 && controller.playbackState != Player.STATE_IDLE) {
            controller.currentPosition
        } else {
            C.TIME_UNSET
        }
        val wasPlaying = controller.isPlaying && currentIndex >= 0

        controller.setMediaItems(mediaItems, startIndex, startPosition)
        controller.prepare()
        if (wasPlaying) controller.play()
    }

    private fun AudioItem.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(Uri.fromFile(File(filePath)))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(author)
                    .setArtworkUri(coverImagePath?.let { Uri.fromFile(File(it)) })
                    .build()
            )
            .build()

    fun onAudioClicked(audioId: Long) {
        if (_activeAudioId.value == audioId) {
            _activeAudioId.value = null
            return
        }

        _activeAudioId.value = audioId
        val controller = mediaController ?: return
        if (!controller.playWhenReady ||
            controller.playbackState == Player.STATE_IDLE ||
            controller.playbackState == Player.STATE_ENDED ||
            controller.currentMediaItem?.mediaId == audioId.toString()
        ) return

        val targetIndex = audioList.value.indexOfFirst { it.id == audioId }
        if (targetIndex in 0 until controller.mediaItemCount &&
            controller.getMediaItemAt(targetIndex).mediaId == audioId.toString()
        ) {
            controller.seekTo(targetIndex, C.TIME_UNSET)
        }
    }

    fun onPlayPauseClicked() {
        val controller = mediaController ?: return
        val activeId = _activeAudioId.value ?: return
        val targetIndex = audioList.value.indexOfFirst { it.id == activeId }
        if (targetIndex < 0) return

        if (controller.currentMediaItemIndex == targetIndex &&
            controller.playbackState != Player.STATE_IDLE &&
            controller.playbackState != Player.STATE_ENDED
        ) {
            if (controller.isPlaying) controller.pause() else controller.play()
        } else {
            controller.seekTo(targetIndex, C.TIME_UNSET)
            controller.play()
        }
    }

    fun deleteAudio(audio: AudioItem) {
        viewModelScope.launch {
            audioDao.deleteAudio(audio)
            File(audio.filePath).takeIf { it.exists() }?.delete()
            if (_activeAudioId.value == audio.id) {
                mediaController?.stop()
                _activeAudioId.value = null
            }
        }
    }

    fun playNext() = playAdjacent(1)

    fun playPrevious() = playAdjacent(-1)

    private fun playAdjacent(offset: Int) {
        val controller = mediaController ?: return
        val activeId = _activeAudioId.value ?: return
        val items = audioList.value
        val activeIndex = items.indexOfFirst { it.id == activeId }
        if (activeIndex < 0) return

        val targetIndex = activeIndex + offset
        val target = items.getOrNull(targetIndex) ?: return
        if (targetIndex >= controller.mediaItemCount ||
            controller.getMediaItemAt(targetIndex).mediaId != target.id.toString()
        ) return

        controller.seekTo(targetIndex, C.TIME_UNSET)
        _activeAudioId.value = target.id
        controller.play()
    }

    fun updateAudioMetadata(audio: AudioItem, newTitle: String, newAuthor: String) {
        viewModelScope.launch {
            val updated = audio.copy(title = newTitle, author = newAuthor)
            audioDao.updateAudio(updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
    }
}
