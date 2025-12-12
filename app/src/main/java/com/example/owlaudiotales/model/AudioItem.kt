package com.example.owlaudiotales.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_items")
data class AudioItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val filePath: String,
    val duration: Int,
    val createdAt: Long,
    val coverImagePath: String? = null
)