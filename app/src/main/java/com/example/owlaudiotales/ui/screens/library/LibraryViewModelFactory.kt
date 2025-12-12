package com.example.owlaudiotales.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.owlaudiotales.data.local.AudioDao

class LibraryViewModelFactory(
    private val audioDao: AudioDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            return LibraryViewModel(audioDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
