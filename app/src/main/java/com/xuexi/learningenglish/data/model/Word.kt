package com.xuexi.learningenglish.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
 data class Word(
    @SerialName("word") val word: String,
    @SerialName("phonetic") val phonetic: String,
    @SerialName("partOfSpeech") val partOfSpeech: String,
    @SerialName("meaning") val meaning: String,
    @SerialName("source") val source: String,
    @SerialName("blocks") val blocks: List<String>,
    @SerialName("sounds") val sounds: List<String> = emptyList(),
    @SerialName("types") val types: List<String> = emptyList()
)
