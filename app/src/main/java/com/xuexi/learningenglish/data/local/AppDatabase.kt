package com.xuexi.learningenglish.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WordProgressEntity::class, PointTransactionEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordProgressDao(): WordProgressDao
    abstract fun pointTransactionDao(): PointTransactionDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN correctStreak INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN practiceCorrectDays INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN lastPracticeDay INTEGER NOT NULL DEFAULT -1"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS point_transaction (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        delta INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        word TEXT,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN hasEnteredDailyLearning INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN dailyLearningCount INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN firstLearnedAt INTEGER NOT NULL DEFAULT -1"
                )
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN lastLearnedAt INTEGER NOT NULL DEFAULT -1"
                )
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN reviewAppearCount INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN wrongBookEntryCount INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE word_progress ADD COLUMN wrongBookResetCount INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learning_english.db"
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6
                ).build().also { INSTANCE = it }
            }
        }
    }
}
