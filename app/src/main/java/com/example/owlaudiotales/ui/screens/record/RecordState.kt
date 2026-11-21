package com.example.owlaudiotales.ui.screens.record

data class RecordState(
    val isStarting: Boolean = false,
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val isSaving: Boolean = false,
    val savedTitle: String? = null,
    val filePath: String? = null,
    val errorMessage: String? = null,
    val showPermissionSettings: Boolean = false
)
