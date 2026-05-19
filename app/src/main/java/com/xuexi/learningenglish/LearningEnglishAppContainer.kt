package com.xuexi.learningenglish

import android.content.Context
import com.xuexi.learningenglish.data.local.AppDatabase
import com.xuexi.learningenglish.data.repository.WordRepository

class LearningEnglishAppContainer(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val repository = WordRepository(
        context = context,
        progressDao = database.wordProgressDao()
    )
}
