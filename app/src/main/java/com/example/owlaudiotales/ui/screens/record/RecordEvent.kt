package com.example.owlaudiotales.ui.screens.record

sealed class RecordEvent {
    object StartRecording : RecordEvent()
    object PauseRecording : RecordEvent()
    object ResumeRecording : RecordEvent()
    object StopRecording : RecordEvent()
    object DiscardRecording : RecordEvent()
    object ClearError : RecordEvent()
    object DismissPermissionSettings : RecordEvent()
    data class RecordingFailed(val message: String) : RecordEvent()
    data class PermissionDenied(val canShowRationale: Boolean) : RecordEvent()
}