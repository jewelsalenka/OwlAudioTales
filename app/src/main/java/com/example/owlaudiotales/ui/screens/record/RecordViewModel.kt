package com.example.owlaudiotales.ui.screens.record

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.model.AudioItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordViewModel(
    private val recordingsDir: File,
    private val audioDao: AudioDao
) : ViewModel() {

    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    private var outputFile: File? = null
    private var startTime: Long = 0L

    fun onEvent(event: RecordEvent) {
        when (event) {
            is RecordEvent.StartRecording -> startRecording()
            is RecordEvent.PauseRecording -> pauseRecording()
            is RecordEvent.ResumeRecording -> resumeRecording()
            is RecordEvent.StopRecording -> stopRecording()
            is RecordEvent.RecordingFailed -> _state.value = _state.value.copy(errorMessage = event.message)
        }
    }

    private fun startRecording() {
        try {
            if (!recordingsDir.exists()) recordingsDir.mkdirs()
            outputFile = File(recordingsDir, "recording_${System.currentTimeMillis()}.m4a")
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }
            startTime = System.currentTimeMillis()
            _state.update {
                it.copy(isRecording = true, isPaused = false, filePath = outputFile!!.absolutePath)
            }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = "Помилка запису: ${e.message}") }
            Log.e("RecordVM", "Start error", e)
        }

        val outputDir = File(recordingsDir, "recordings")
        if (!outputDir.exists()) outputDir.mkdirs()
    }

    private fun pauseRecording() {
        try {
            recorder?.pause()
            _state.update { it.copy(isPaused = true) }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = "Помилка паузи: ${e.message}") }
        }
    }

    private fun resumeRecording() {
        try {
            recorder?.resume()
            _state.update { it.copy(isPaused = false) }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = "Помилка відновлення: ${e.message}") }
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            _state.update {
                it.copy(isRecording = false, isPaused = false, errorMessage = null)
            }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = "Помилка зупинки: ${e.message}") }
        }
    }

    fun saveMetadata(title: String, author: String, coverPath: String?) {
        val filePath = state.value.filePath ?: return
        val durationSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val item = AudioItem(
            title = title,
            author = author,
            filePath = filePath,
            duration = durationSec,
            createdAt = System.currentTimeMillis(),
            coverImagePath = coverPath
        )
        viewModelScope.launch {
            audioDao.insertAudio(item)
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorder?.release()
        recorder = null
    }

}