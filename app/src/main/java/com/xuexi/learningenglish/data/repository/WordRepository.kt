package com.xuexi.learningenglish.data.repository

import android.content.Context
import com.xuexi.learningenglish.data.local.WordProgressDao
import com.xuexi.learningenglish.data.local.WordProgressEntity
import com.xuexi.learningenglish.data.local.WordStatus
import com.xuexi.learningenglish.data.model.PracticeQuestion
import com.xuexi.learningenglish.data.model.PracticeQuestionType
import com.xuexi.learningenglish.data.model.Word
import com.xuexi.learningenglish.data.model.WordCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.serialization.json.Json
import kotlin.random.Random

class WordRepository(
    private val context: Context,
    private val progressDao: WordProgressDao
) {
    private var cachedWords: List<Word>? = null
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getWords(forceRefresh: Boolean = false): List<Word> = withContext(Dispatchers.IO) {
        if (cachedWords == null || forceRefresh) {
            val payload = context.assets.open("words.json").bufferedReader().use { it.readText() }
            cachedWords = json.decodeFromString(payload)
        }
        cachedWords.orEmpty()
    }

    suspend fun getWordCards(query: String = ""): List<WordCard> = withContext(Dispatchers.IO) {
        val words = getWords()
        val progressMap = progressDao.getAll().associateBy { it.word.lowercase() }
        words
            .filter { query.isBlank() || it.word.contains(query, ignoreCase = true) || it.meaning.contains(query, ignoreCase = true) }
            .map { word ->
                WordCard(word = word, progress = progressMap[word.word.lowercase()])
            }
    }

    suspend fun getWordCard(word: String): WordCard? = withContext(Dispatchers.IO) {
        val target = getWords().firstOrNull { it.word.equals(word, ignoreCase = true) } ?: return@withContext null
        val progress = progressDao.getByWord(word)
        WordCard(word = target, progress = progress)
    }

    suspend fun markRemembered(word: Word): WordProgressEntity = withContext(Dispatchers.IO) {
        val current = progressDao.getByWord(word.word)
        val reviewCount = (current?.reviewCount ?: 0) + 1
        val nextStatus = if (current?.status == WordStatus.WRONG.name) {
            WordStatus.WRONG
        } else {
            WordStatus.MASTERED
        }
        val persisted = WordProgressEntity(
            word = word.word,
            status = nextStatus.name,
            reviewCount = reviewCount,
            wrongCount = current?.wrongCount ?: 0,
            correctStreak = if (nextStatus == WordStatus.WRONG) current?.correctStreak ?: 0 else 0,
            practiceCorrectDays = current?.practiceCorrectDays ?: 0,
            lastPracticeDay = current?.lastPracticeDay ?: -1,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.upsert(persisted)
        persisted
    }

    suspend fun markForgotten(word: Word): WordProgressEntity = withContext(Dispatchers.IO) {
        val current = progressDao.getByWord(word.word)
        val persisted = WordProgressEntity(
            word = word.word,
            status = WordStatus.WRONG.name,
            reviewCount = (current?.reviewCount ?: 0) + 1,
            wrongCount = (current?.wrongCount ?: 0) + 1,
            correctStreak = 0,
            practiceCorrectDays = 0,
            lastPracticeDay = current?.lastPracticeDay ?: -1,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.upsert(persisted)
        persisted
    }

    suspend fun getDailyLearningSet(size: Int): List<WordCard> = withContext(Dispatchers.IO) {
        val words = getWords()
        val progressMap = progressDao.getAll().associateBy { it.word.lowercase() }
        val wrongWords = words.filter { progressMap[it.word.lowercase()]?.status == WordStatus.WRONG.name }
        val wrongShuffled = wrongWords.shuffled(Random(System.currentTimeMillis()))
        val selectedWrong = wrongShuffled.take(size)
        val wrongKeys = selectedWrong.map { it.word.lowercase() }.toSet()
        val remaining = words
            .filterNot { wrongKeys.contains(it.word.lowercase()) }
            .shuffled(Random(System.currentTimeMillis()))
            .take((size - selectedWrong.size).coerceAtLeast(0))
        (selectedWrong + remaining).shuffled(Random(System.currentTimeMillis())).map { word ->
            WordCard(word = word, progress = progressMap[word.word.lowercase()])
        }
    }

    suspend fun getWrongWordCards(): List<WordCard> = withContext(Dispatchers.IO) {
        val wrongMap = progressDao.getByStatus(WordStatus.WRONG.name).associateBy { it.word.lowercase() }
        getWords().mapNotNull { word ->
            val progress = wrongMap[word.word.lowercase()] ?: return@mapNotNull null
            WordCard(word = word, progress = progress)
        }
    }

    suspend fun updateStatus(word: Word, status: WordStatus) = withContext(Dispatchers.IO) {
        val current = progressDao.getByWord(word.word)
        val wrongCount = when (status) {
            WordStatus.WRONG -> (current?.wrongCount ?: 0) + 1
            else -> current?.wrongCount ?: 0
        }
        val reviewCount = (current?.reviewCount ?: 0) + 1
        progressDao.upsert(
            WordProgressEntity(
                word = word.word,
                status = status.name,
                reviewCount = reviewCount,
                wrongCount = wrongCount,
                correctStreak = if (status == WordStatus.WRONG) 0 else (current?.correctStreak ?: 0),
                practiceCorrectDays = if (status == WordStatus.WRONG) 0 else (current?.practiceCorrectDays ?: 0),
                lastPracticeDay = current?.lastPracticeDay ?: -1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getWrongWords(): List<WordCard> = withContext(Dispatchers.IO) {
        getWrongWordCards()
    }

    suspend fun buildPracticeQuestions(todayCards: List<WordCard>): List<PracticeQuestion> = withContext(Dispatchers.IO) {
        val wrongCards = getWrongWordCards()
        val merged = LinkedHashMap<String, WordCard>()
        todayCards.forEach { merged[it.word.word.lowercase()] = it }
        wrongCards.forEach { merged[it.word.word.lowercase()] = it }
        val random = Random(System.currentTimeMillis())
        merged.values.shuffled(random).map { card ->
            createPracticeQuestion(card.word, random)
        }
    }

    suspend fun recordPracticeResult(word: Word, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        val current = progressDao.getByWord(word.word)
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val reviewCount = (current?.reviewCount ?: 0) + 1
        val wrongCount = if (isCorrect) {
            current?.wrongCount ?: 0
        } else {
            (current?.wrongCount ?: 0) + 1
        }
        val practiceCorrectDays = when {
            !isCorrect -> 0
            current?.status == WordStatus.WRONG.name && current.lastPracticeDay != today -> {
                (current.practiceCorrectDays + 1).coerceAtMost(4)
            }
            current?.status == WordStatus.WRONG.name -> current.practiceCorrectDays
            else -> current?.practiceCorrectDays ?: 0
        }
        val nextStatus = when {
            !isCorrect -> WordStatus.WRONG
            current?.status == WordStatus.WRONG.name && practiceCorrectDays >= 4 -> WordStatus.MASTERED
            current?.status == WordStatus.WRONG.name -> WordStatus.WRONG
            current == null -> WordStatus.LEARNING
            else -> current.status.let(WordStatus::valueOf)
        }
        progressDao.upsert(
            WordProgressEntity(
                word = word.word,
                status = nextStatus.name,
                reviewCount = reviewCount,
                wrongCount = wrongCount,
                correctStreak = if (!isCorrect) 0 else current?.correctStreak ?: 0,
                practiceCorrectDays = if (nextStatus == WordStatus.WRONG) practiceCorrectDays else 0,
                lastPracticeDay = today,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun createPracticeQuestion(word: Word, random: Random): PracticeQuestion {
        val type = if (word.word.length < 4 || random.nextBoolean()) {
            PracticeQuestionType.SPELL_FROM_MEANING
        } else {
            PracticeQuestionType.COMPLETE_MISSING_LETTERS
        }
        return when (type) {
            PracticeQuestionType.SPELL_FROM_MEANING -> PracticeQuestion(
                word = word,
                type = type,
                prompt = "根据中文写出完整英文单词",
                clue = "${word.meaning} (${word.partOfSpeech})",
                expectedAnswer = word.word
            )

            PracticeQuestionType.COMPLETE_MISSING_LETTERS -> {
                val hiddenLength = when {
                    word.word.length >= 8 -> 3
                    word.word.length >= 5 -> 2
                    else -> 1
                }
                val start = 1 + random.nextInt((word.word.length - hiddenLength - 1).coerceAtLeast(1))
                val end = start + hiddenLength
                val missingPart = word.word.substring(start, end)
                val maskedWord = buildString {
                    append(word.word.substring(0, start))
                    append("_".repeat(hiddenLength))
                    append(word.word.substring(end))
                }
                PracticeQuestion(
                    word = word,
                    type = type,
                    prompt = "按顺序补全缺少的字母",
                    clue = "$maskedWord\n中文：${word.meaning}",
                    expectedAnswer = missingPart
                )
            }
        }
    }
}
