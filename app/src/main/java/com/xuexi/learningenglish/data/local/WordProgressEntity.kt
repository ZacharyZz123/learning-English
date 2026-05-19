package com.xuexi.learningenglish.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_progress")
data class WordProgressEntity(
    @PrimaryKey val word: String,
    val status: String = WordStatus.NEW.name,
    val reviewCount: Int = 0,
    val wrongCount: Int = 0,
    val wrongBookEntryCount: Int = 0,
    val wrongBookResetCount: Int = 0,
    val correctStreak: Int = 0,
    val practiceCorrectDays: Int = 0,
    val hasEnteredDailyLearning: Boolean = false,
    val dailyLearningCount: Int = 0,
    val firstLearnedAt: Long = -1,
    val lastLearnedAt: Long = -1,
    val reviewAppearCount: Int = 0,
    val lastPracticeDay: Long = -1,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class WordStatus {
    NEW,
    LEARNING,
    MASTERED,
    WRONG
}
