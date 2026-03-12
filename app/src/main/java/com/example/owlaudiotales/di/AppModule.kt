package com.example.owlaudiotales.di

import android.content.Context
import androidx.room.Room
import com.example.owlaudiotales.data.local.AppDatabase
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.datastore.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "audio_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAudioDao(database: AppDatabase): AudioDao {
        return database.audioDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
