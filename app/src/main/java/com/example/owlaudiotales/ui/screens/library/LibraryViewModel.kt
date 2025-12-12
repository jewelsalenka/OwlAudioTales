package com.example.owlaudiotales.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.model.AudioItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LibraryViewModel(
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

    private var currentIndex: Int = -1

    fun onAudioClicked(audioId: Long) {
        _activeAudioId.value = if (_activeAudioId.value == audioId) null else audioId
        updateCurrentIndex()
    }

    fun deleteAudio(audio: AudioItem) {
        viewModelScope.launch {
            audioDao.deleteAudio(audio)

            val file = File(audio.filePath)
            if (file.exists()) file.delete()

            if (_activeAudioId.value == audio.id) {
                _activeAudioId.value = null
                currentIndex = -1
            } else {
                updateCurrentIndex()
            }
        }
    }

    fun playNext() {
        val list = audioList.value
        if (currentIndex in list.indices) {
            val nextIndex = (currentIndex + 1) % list.size
            _activeAudioId.value = list[nextIndex].id
            currentIndex = nextIndex
        }
    }

    fun playPrevious() {
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
}
