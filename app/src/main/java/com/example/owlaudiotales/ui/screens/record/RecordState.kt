package com.example.owlaudiotales.ui.screens.record

data class RecordState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val filePath: String? = null,
    val errorMessage: String? = null
)