package com.xuexi.learningenglish.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordProgressDao {
    @Query("SELECT * FROM word_progress")
    suspend fun getAll(): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getByWord(word: String): WordProgressEntity?

    @Query("SELECT * FROM word_progress WHERE status = :status ORDER BY updatedAt DESC")
    suspend fun getByStatus(status: String): List<WordProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: WordProgressEntity)
}
