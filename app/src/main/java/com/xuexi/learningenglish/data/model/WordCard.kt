package com.xuexi.learningenglish.data.model

import com.xuexi.learningenglish.data.local.WordProgressEntity

data class WordCard(
    val word: Word,
    val progress: WordProgressEntity?
)
