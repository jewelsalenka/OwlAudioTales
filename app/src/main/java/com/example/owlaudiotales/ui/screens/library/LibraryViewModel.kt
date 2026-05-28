package com.example.owlaudiotales.ui.screens.library

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.model.AudioItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
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

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex: Int = -1

    fun onAudioClicked(audioId: Long) {
        if (_activeAudioId.value == audioId) {
            _activeAudioId.value = null
        } else {
            releasePlayer()
            _activeAudioId.value = audioId
        }
        updateCurrentIndex()
    }

    fun onPlayPauseClicked(audioPath: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                setOnPreparedListener {
                    it.start()
                    _isPlaying.value = true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                }
                prepareAsync()
            }
        } else {
            if (_isPlaying.value) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
            }
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun deleteAudio(audio: AudioItem) {
        viewModelScope.launch {
            audioDao.deleteAudio(audio)

            val file = File(audio.filePath)
            if (file.exists()) file.delete()

            if (_activeAudioId.value == audio.id) {
                releasePlayer()
                _activeAudioId.value = null
                currentIndex = -1
            } else {
                updateCurrentIndex()
            }
        }
    }

    fun playNext() {
        releasePlayer()
        val list = audioList.value
        if (currentIndex in list.indices) {
            val nextIndex = (currentIndex + 1) % list.size
            _activeAudioId.value = list[nextIndex].id
            currentIndex = nextIndex
        }
    }

    fun playPrevious() {
        releasePlayer()
        val list = audioList.value
        if (currentIndex in list.indices) {
            val prevIndex = if (currentIndex - 1 < 0) list.lastIndex else currentIndex - 1
            _activeAudioId.value = list[prevIndex].id
            currentIndex = prevIndex
        }
    }

    private fun updateCurrentIndex() {
        val list = audioList.value
        currentIndex = list.indexOfFirst { it.id == _activeAudioId.value }
    }


    fun updateAudioMetadata(audio: AudioItem, newTitle: String, newAuthor: String) {
        viewModelScope.launch {
            val updated = audio.copy(title = newTitle, author = newAuthor)
            audioDao.updateAudio(updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
