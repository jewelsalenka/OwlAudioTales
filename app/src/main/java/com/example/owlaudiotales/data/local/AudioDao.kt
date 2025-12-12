package com.example.owlaudiotales.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.example.owlaudiotales.model.AudioItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(item: AudioItem): Long

    @Delete
    suspend fun deleteAudio(item: AudioItem)

    @Query("SELECT * FROM audio_items ORDER BY createdAt DESC")
    fun getAllAudio(): Flow<List<AudioItem>>

    @Query("SELECT * FROM audio_items WHERE id = :id")
    suspend fun getAudioById(id: Long): AudioItem?

    @Update
    suspend fun updateAudio(audioItem: AudioItem)

}