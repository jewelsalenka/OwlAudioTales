package com.example.owlaudiotales.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.owlaudiotales.model.AudioItem

@Database(
    entities = [AudioItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioDao(): AudioDao
}