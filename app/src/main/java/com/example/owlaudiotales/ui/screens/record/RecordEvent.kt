package com.example.owlaudiotales.ui.screens.record

sealed class RecordEvent {
    object StartRecording : RecordEvent()
    object PauseRecording : RecordEvent()
    object ResumeRecording : RecordEvent()
    object StopRecording : RecordEvent()
    data class RecordingFailed(val message: String) : RecordEvent()
}