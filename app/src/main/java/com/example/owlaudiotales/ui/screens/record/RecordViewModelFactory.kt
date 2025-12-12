package com.example.owlaudiotales.ui.screens.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.owlaudiotales.data.local.AudioDao
import java.io.File

class RecordViewModelFactory(
    private val recordingsDir: File,
    private val audioDao: AudioDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(recordingsDir, audioDao) as T
    }
}