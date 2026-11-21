package com.example.owlaudiotales.ui.screens.record

import android.content.ComponentName
import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.concurrent.futures.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.example.owlaudiotales.R
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.model.AudioItem
import com.example.owlaudiotales.service.AudioPlaybackService
import com.example.owlaudiotales.service.RecordingPlaybackCommand
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioDao: AudioDao
) : ViewModel() {

    private val recordingsDir = File(context.filesDir, "recordings")

    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var startJob: Job? = null
    private var playbackControllerFuture: ListenableFuture<MediaController>? = null
    private var startTime: Long = 0L
    private var pauseStartTime: Long = 0L
    private var accumulatedPauseMs: Long = 0L
    private var recordingDurationSec: Int = 0

    fun onEvent(event: RecordEvent) {
        when (event) {
            is RecordEvent.StartRecording -> startRecording()
            is RecordEvent.PauseRecording -> pauseRecording()
            is RecordEvent.ResumeRecording -> resumeRecording()
            is RecordEvent.StopRecording -> stopRecording()
            is RecordEvent.DiscardRecording -> discardRecording()
            is RecordEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is RecordEvent.DismissPermissionSettings -> _state.update { it.copy(showPermissionSettings = false) }
            is RecordEvent.RecordingFailed -> _state.update { it.copy(errorMessage = event.message) }
            is RecordEvent.PermissionDenied -> handlePermissionDenied(event.canShowRationale)
        }
    }

    private fun startRecording() {
        val current = state.value
        if (current.isStarting || current.isRecording || current.filePath != null || current.savedTitle != null) return

        _state.update { it.copy(isStarting = true, errorMessage = null) }
        startJob = viewModelScope.launch {
            var file: File? = null
            try {
                val token = SessionToken(context, ComponentName(context, AudioPlaybackService::class.java))
                val future = MediaController.Builder(context, token).buildAsync()
                playbackControllerFuture = future
                val result = withTimeoutOrNull(10.seconds) {
                    future.await().sendCustomCommand(RecordingPlaybackCommand.begin, Bundle()).await()
                }
                check(result?.resultCode == SessionResult.RESULT_SUCCESS) {
                    context.getString(R.string.error_recording_pause_playback)
                }

                recordingsDir.mkdirs()
                val output = File.createTempFile("recording_", ".m4a", recordingsDir)
                file = output
                val newRecorder = MediaRecorder()
                recorder = newRecorder
                newRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(output.absolutePath)
                    prepare()
                    start()
                }
                startTime = SystemClock.elapsedRealtime()
                accumulatedPauseMs = 0L
                _state.update { it.copy(isRecording = true, isPaused = false, filePath = output.absolutePath) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = context.getString(R.string.error_recording_start, e.message.orEmpty()))
                }
                Log.e("RecordVM", "Start error", e)
            } finally {
                if (!state.value.isRecording) {
                    releaseRecordingResources()
                    file?.delete()
                }
                _state.update { it.copy(isStarting = false) }
            }
        }
    }

    private fun pauseRecording() {
        try {
            recorder?.pause()
            pauseStartTime = SystemClock.elapsedRealtime()
            _state.update { it.copy(isPaused = true) }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = context.getString(R.string.error_recording_pause, e.message ?: "")) }
        }
    }

    private fun resumeRecording() {
        try {
            recorder?.resume()
            accumulatedPauseMs += SystemClock.elapsedRealtime() - pauseStartTime
            _state.update { it.copy(isPaused = false) }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = context.getString(R.string.error_recording_resume, e.message ?: "")) }
        }
    }

    private fun stopRecording() {
        val activeRecorder = recorder ?: return
        try {
            val endTime = if (state.value.isPaused) pauseStartTime else SystemClock.elapsedRealtime()
            activeRecorder.stop()
            recordingDurationSec = ((endTime - startTime - accumulatedPauseMs) / 1000).toInt()
            _state.update {
                it.copy(isRecording = false, isPaused = false, errorMessage = null)
            }
        } catch (e: Exception) {
            state.value.filePath?.let { File(it).delete() }
            _state.update {
                it.copy(
                    isRecording = false,
                    isPaused = false,
                    filePath = null,
                    errorMessage = context.getString(R.string.error_recording_stop, e.message.orEmpty())
                )
            }
        } finally {
            releaseRecordingResources()
        }
    }

    private fun discardRecording() {
        if (state.value.isSaving) return
        startJob?.cancel()
        releaseRecordingResources()
        state.value.filePath?.let { File(it).delete() }
        _state.update { it.copy(isStarting = false, isRecording = false, isPaused = false, filePath = null) }
    }

    private fun handlePermissionDenied(canShowRationale: Boolean) {
        if (canShowRationale) {
            _state.update { it.copy(errorMessage = context.getString(R.string.error_mic_permission_denied)) }
        } else {
            _state.update { it.copy(showPermissionSettings = true) }
        }
    }

    fun saveMetadata(title: String, author: String, coverPath: String?) {
        val current = state.value
        if (current.isRecording || current.isSaving) return
        val filePath = current.filePath ?: return
        val item = AudioItem(
            title = title,
            author = author,
            filePath = filePath,
            duration = recordingDurationSec,
            createdAt = System.currentTimeMillis(),
            coverImagePath = coverPath
        )
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                audioDao.insertAudio(item)
                _state.update { it.copy(filePath = null, savedTitle = title, isSaving = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(isSaving = false, errorMessage = context.getString(R.string.error_recording_save))
                }
                Log.e("RecordVM", "Save error", e)
            }
        }
    }

    private fun releaseRecordingResources() {
        try {
            recorder?.release()
        } finally {
            recorder = null
            playbackControllerFuture?.let { MediaController.releaseFuture(it) }
            playbackControllerFuture = null
        }
    }

    override fun onCleared() {
        releaseRecordingResources()
        if (state.value.isRecording) state.value.filePath?.let { File(it).delete() }
        super.onCleared()
    }
}
