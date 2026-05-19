package com.xuexi.learningenglish.data.repository

import android.content.Context
import com.xuexi.learningenglish.data.local.PointTransactionDao
import com.xuexi.learningenglish.data.local.PointTransactionEntity
import com.xuexi.learningenglish.data.local.PointTransactionType
import com.xuexi.learningenglish.data.local.WordProgressDao
import com.xuexi.learningenglish.data.local.WordProgressEntity
import com.xuexi.learningenglish.data.local.WordStatus
import com.xuexi.learningenglish.data.model.PointChangeResult
import com.xuexi.learningenglish.data.model.PracticeQuestion
import com.xuexi.learningenglish.data.model.PracticeQuestionSource
import com.xuexi.learningenglish.data.model.PracticeSessionMode
import com.xuexi.learningenglish.data.model.PracticeQuestionType
import com.xuexi.learningenglish.data.model.RedeemResult
import com.xuexi.learningenglish.data.model.Word
import com.xuexi.learningenglish.data.model.WordCard
import com.xuexi.learningenglish.data.model.WordErrorRankItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.serialization.json.Json
import kotlin.random.Random

class WordRepository(
    private val context: Context,
    private val progressDao: WordProgressDao,
    private val pointTransactionDao: PointTransactionDao
) {
    private companion object {
        const val STATS_PREFS = "learning_stats"
        const val KEY_CONTINUOUS_LEARNING_DAYS = "continuous_learning_days"
        const val KEY_LAST_COMPLETED_DAY = "last_completed_day"
    }

    private var cachedWords: List<Word>? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val statsPrefs by lazy {
        context.getSharedPreferences(STATS_PREFS, Context.MODE_PRIVATE)
    }

    suspend fun getWords(forceRefresh: Boolean = false): List<Word> = withContext(Dispatchers.IO) {
        if (cachedWords == null || forceRefresh) {
            val payload = context.assets.open("words.json").bufferedReader().use { it.readText() }
            cachedWords = json.decodeFromString(payload)
        }
        cachedWords.orEmpty()
    }

    suspend fun getWordCount(): Int = withContext(Dispatchers.IO) {
        getWords().size
    }

    suspend fun getPointBalance(): Int = withContext(Dispatchers.IO) {
        pointTransactionDao.getBalance()
    }

    suspend fun getPointTransactions(): List<PointTransactionEntity> = withContext(Dispatchers.IO) {
        pointTransactionDao.getAll()
    }

    suspend fun getContinuousLearningDays(): Int = withContext(Dispatchers.IO) {
        statsPrefs.getInt(KEY_CONTINUOUS_LEARNING_DAYS, 0)
    }

    suspend fun markDailyLearningCompleted(): Int = withContext(Dispatchers.IO) {
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val lastCompletedDay = statsPrefs.getLong(KEY_LAST_COMPLETED_DAY, Long.MIN_VALUE)
        if (lastCompletedDay == today) {
            return@withContext statsPrefs.getInt(KEY_CONTINUOUS_LEARNING_DAYS, 0)
        }
        val nextDays = statsPrefs.getInt(KEY_CONTINUOUS_LEARNING_DAYS, 0) + 1
        statsPrefs.edit()
            .putInt(KEY_CONTINUOUS_LEARNING_DAYS, nextDays)
            .putLong(KEY_LAST_COMPLETED_DAY, today)
            .apply()
        nextDays
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
            wrongBookEntryCount = current?.wrongBookEntryCount ?: 0,
            wrongBookResetCount = current?.wrongBookResetCount ?: 0,
            correctStreak = if (nextStatus == WordStatus.WRONG) current?.correctStreak ?: 0 else 0,
            practiceCorrectDays = current?.practiceCorrectDays ?: 0,
            hasEnteredDailyLearning = current?.hasEnteredDailyLearning ?: false,
            dailyLearningCount = current?.dailyLearningCount ?: 0,
            firstLearnedAt = current?.firstLearnedAt ?: -1,
            lastLearnedAt = current?.lastLearnedAt ?: -1,
            reviewAppearCount = current?.reviewAppearCount ?: 0,
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
            wrongBookEntryCount = if (current?.status == WordStatus.WRONG.name) {
                current.wrongBookEntryCount
            } else {
                (current?.wrongBookEntryCount ?: 0) + 1
            },
            wrongBookResetCount = current?.wrongBookResetCount ?: 0,
            correctStreak = 0,
            practiceCorrectDays = 0,
            hasEnteredDailyLearning = current?.hasEnteredDailyLearning ?: false,
            dailyLearningCount = current?.dailyLearningCount ?: 0,
            firstLearnedAt = current?.firstLearnedAt ?: -1,
            lastLearnedAt = current?.lastLearnedAt ?: -1,
            reviewAppearCount = current?.reviewAppearCount ?: 0,
            lastPracticeDay = current?.lastPracticeDay ?: -1,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.upsert(persisted)
        persisted
    }

    suspend fun completeDailyLearningWord(word: Word): WordProgressEntity = withContext(Dispatchers.IO) {
        val current = progressDao.getByWord(word.word)
        val nextStatus = when (current?.status) {
            WordStatus.WRONG.name -> WordStatus.WRONG.name
            WordStatus.MASTERED.name -> WordStatus.MASTERED.name
            else -> WordStatus.LEARNING.name
        }
        val persisted = WordProgressEntity(
            word = word.word,
            status = nextStatus,
            reviewCount = current?.reviewCount ?: 0,
            wrongCount = current?.wrongCount ?: 0,
            wrongBookEntryCount = current?.wrongBookEntryCount ?: 0,
            wrongBookResetCount = current?.wrongBookResetCount ?: 0,
            correctStreak = current?.correctStreak ?: 0,
            practiceCorrectDays = current?.practiceCorrectDays ?: 0,
            hasEnteredDailyLearning = current?.hasEnteredDailyLearning ?: false,
            dailyLearningCount = current?.dailyLearningCount ?: 0,
            firstLearnedAt = current?.firstLearnedAt ?: -1,
            lastLearnedAt = current?.lastLearnedAt ?: -1,
            reviewAppearCount = current?.reviewAppearCount ?: 0,
            lastPracticeDay = current?.lastPracticeDay ?: -1,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.upsert(persisted)
        persisted
    }

    suspend fun getDailyLearningSet(size: Int): List<WordCard> = withContext(Dispatchers.IO) {
        val words = getWords()
        val progressMap = progressDao.getAll().associateBy { it.word.lowercase() }
        words
            .filter { progressMap[it.word.lowercase()]?.hasEnteredDailyLearning != true }
            .shuffled(Random(System.currentTimeMillis()))
            .take(size)
            .map { word ->
            WordCard(word = word, progress = progressMap[word.word.lowercase()])
        }
    }

    suspend fun markWordsEnteredDailyLearning(words: List<Word>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        words.forEach { word ->
            val current = progressDao.getByWord(word.word)
            progressDao.upsert(
                WordProgressEntity(
                    word = word.word,
                    status = current?.status ?: WordStatus.NEW.name,
                    reviewCount = current?.reviewCount ?: 0,
                    wrongCount = current?.wrongCount ?: 0,
                    wrongBookEntryCount = current?.wrongBookEntryCount ?: 0,
                    wrongBookResetCount = current?.wrongBookResetCount ?: 0,
                    correctStreak = current?.correctStreak ?: 0,
                    practiceCorrectDays = current?.practiceCorrectDays ?: 0,
                    hasEnteredDailyLearning = true,
                    dailyLearningCount = (current?.dailyLearningCount ?: 0) + 1,
                    firstLearnedAt = if ((current?.firstLearnedAt ?: -1) > 0) current!!.firstLearnedAt else now,
                    lastLearnedAt = now,
                    reviewAppearCount = current?.reviewAppearCount ?: 0,
                    lastPracticeDay = current?.lastPracticeDay ?: -1,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun getReviewPracticeSet(limit: Int = 100): List<WordCard> = withContext(Dispatchers.IO) {
        val allProgress = progressDao.getAll()
        val learnedProgressMap = allProgress
            .filter { it.hasEnteredDailyLearning }
            .associateBy { it.word.lowercase() }
        val learnedCards = getWords().mapNotNull { word ->
            val progress = learnedProgressMap[word.word.lowercase()] ?: return@mapNotNull null
            WordCard(word = word, progress = progress)
        }
        learnedCards
            .groupBy { it.progress?.reviewAppearCount ?: 0 }
            .toSortedMap()
            .values
            .flatMap { group -> group.shuffled(Random(System.currentTimeMillis())) }
            .take(limit)
    }

    suspend fun markReviewWordCompleted(word: Word) = withContext(Dispatchers.IO) {
        val current = progressDao.getByWord(word.word) ?: return@withContext
        progressDao.upsert(
            current.copy(
                reviewAppearCount = current.reviewAppearCount + 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getLearnedWordCount(): Int = withContext(Dispatchers.IO) {
        progressDao.getAll().count { it.hasEnteredDailyLearning }
    }

    suspend fun getUnlearnedWordCount(): Int = withContext(Dispatchers.IO) {
        (getWords().size - getLearnedWordCount()).coerceAtLeast(0)
    }

    suspend fun getErrorRanking(): List<WordErrorRankItem> = withContext(Dispatchers.IO) {
        progressDao.getAll()
            .mapNotNull { progress ->
                val errorCount = progress.wrongBookEntryCount + progress.wrongBookResetCount
                if (errorCount <= 0) {
                    null
                } else {
                    WordErrorRankItem(word = progress.word, errorCount = errorCount)
                }
            }
            .sortedWith(
                compareByDescending<WordErrorRankItem> { it.errorCount }
                    .thenBy { it.word.lowercase() }
            )
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
                wrongBookEntryCount = if (status == WordStatus.WRONG && current?.status != WordStatus.WRONG.name) {
                    (current?.wrongBookEntryCount ?: 0) + 1
                } else {
                    current?.wrongBookEntryCount ?: 0
                },
                wrongBookResetCount = current?.wrongBookResetCount ?: 0,
                correctStreak = if (status == WordStatus.WRONG) 0 else (current?.correctStreak ?: 0),
                practiceCorrectDays = if (status == WordStatus.WRONG) 0 else (current?.practiceCorrectDays ?: 0),
                hasEnteredDailyLearning = current?.hasEnteredDailyLearning ?: false,
                dailyLearningCount = current?.dailyLearningCount ?: 0,
                firstLearnedAt = current?.firstLearnedAt ?: -1,
                lastLearnedAt = current?.lastLearnedAt ?: -1,
                reviewAppearCount = current?.reviewAppearCount ?: 0,
                lastPracticeDay = current?.lastPracticeDay ?: -1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getWrongWords(): List<WordCard> = withContext(Dispatchers.IO) {
        getWrongWordCards()
    }

    suspend fun buildPracticeQuestions(
        todayCards: List<WordCard>,
        mode: PracticeSessionMode = PracticeSessionMode.DAILY
    ): List<PracticeQuestion> = withContext(Dispatchers.IO) {
        val baseCards = if (mode == PracticeSessionMode.DAILY) {
            val wrongCards = getWrongWordCards()
            val merged = LinkedHashMap<String, WordCard>()
            todayCards.forEach { merged[it.word.word.lowercase()] = it }
            wrongCards.forEach { merged[it.word.word.lowercase()] = it }
            merged.values.toList()
        } else {
            todayCards
        }
        val random = Random(System.currentTimeMillis())
        baseCards.shuffled(random).map { card ->
            createPracticeQuestion(card.word, random)
        }
    }

    suspend fun buildRetryPracticeQuestion(word: Word): PracticeQuestion = withContext(Dispatchers.Default) {
        createPracticeQuestion(word, Random(System.currentTimeMillis()), PracticeQuestionSource.WRONG_RETRY)
    }

    suspend fun recordPracticeResult(word: Word, isCorrect: Boolean): PointChangeResult = withContext(Dispatchers.IO) {
        val current = progressDao.getByWord(word.word)
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val reviewCount = (current?.reviewCount ?: 0) + 1
        val wrongCount = if (isCorrect) {
            current?.wrongCount ?: 0
        } else {
            (current?.wrongCount ?: 0) + 1
        }
        val wrongBookEntryCount = when {
            isCorrect -> current?.wrongBookEntryCount ?: 0
            current?.status == WordStatus.WRONG.name -> current.wrongBookEntryCount
            else -> (current?.wrongBookEntryCount ?: 0) + 1
        }
        val wrongBookResetCount = when {
            isCorrect -> current?.wrongBookResetCount ?: 0
            current?.status == WordStatus.WRONG.name && (current.practiceCorrectDays > 0) -> {
                current.wrongBookResetCount + 1
            }
            else -> current?.wrongBookResetCount ?: 0
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
                wrongBookEntryCount = wrongBookEntryCount,
                wrongBookResetCount = wrongBookResetCount,
                correctStreak = if (!isCorrect) 0 else current?.correctStreak ?: 0,
                practiceCorrectDays = if (nextStatus == WordStatus.WRONG) practiceCorrectDays else 0,
                hasEnteredDailyLearning = current?.hasEnteredDailyLearning ?: false,
                dailyLearningCount = current?.dailyLearningCount ?: 0,
                firstLearnedAt = current?.firstLearnedAt ?: -1,
                lastLearnedAt = current?.lastLearnedAt ?: -1,
                reviewAppearCount = current?.reviewAppearCount ?: 0,
                lastPracticeDay = today,
                updatedAt = System.currentTimeMillis()
            )
        )
        val clearedWrongBook = isCorrect &&
            current?.status == WordStatus.WRONG.name &&
            nextStatus == WordStatus.MASTERED
        val pointChange = when {
            !isCorrect -> PointChangeResult(delta = -1, balance = 0, title = "答错扣分")
            clearedWrongBook -> PointChangeResult(delta = 4, balance = 0, title = "错题本清除奖励")
            else -> PointChangeResult(delta = 2, balance = 0, title = "答对加分")
        }
        pointTransactionDao.insert(
            PointTransactionEntity(
                delta = pointChange.delta,
                type = when {
                    !isCorrect -> PointTransactionType.PRACTICE_WRONG.name
                    clearedWrongBook -> PointTransactionType.WRONG_BOOK_CLEARED.name
                    else -> PointTransactionType.PRACTICE_CORRECT.name
                },
                title = pointChange.title,
                word = word.word
            )
        )
        pointChange.copy(balance = pointTransactionDao.getBalance())
    }

    suspend fun redeemCash(): RedeemResult = withContext(Dispatchers.IO) {
        redeemPoints(cost = 100, type = PointTransactionType.REDEEM_CASH, title = "兑换10元")
    }

    suspend fun redeemCustom(cost: Int): RedeemResult = withContext(Dispatchers.IO) {
        if (cost <= 0) {
            return@withContext RedeemResult(
                success = false,
                message = "请输入有效积分",
                balance = pointTransactionDao.getBalance()
            )
        }
        redeemPoints(
            cost = cost,
            type = PointTransactionType.REDEEM_CUSTOM,
            title = "向父母申请兑换"
        )
    }

    private suspend fun redeemPoints(
        cost: Int,
        type: PointTransactionType,
        title: String
    ): RedeemResult {
        val balance = pointTransactionDao.getBalance()
        if (balance < cost) {
            return RedeemResult(success = false, message = "积分不足", balance = balance)
        }
        pointTransactionDao.insert(
            PointTransactionEntity(
                delta = -cost,
                type = type.name,
                title = title
            )
        )
        val nextBalance = pointTransactionDao.getBalance()
        return RedeemResult(success = true, message = "兑换成功", balance = nextBalance)
    }

    private fun createPracticeQuestion(
        word: Word,
        random: Random,
        source: PracticeQuestionSource = PracticeQuestionSource.NORMAL
    ): PracticeQuestion {
        val type = if (word.word.length < 4 || random.nextBoolean()) {
            PracticeQuestionType.SPELL_FROM_MEANING
        } else {
            PracticeQuestionType.COMPLETE_MISSING_LETTERS
        }
        return when (type) {
            PracticeQuestionType.SPELL_FROM_MEANING -> PracticeQuestion(
                id = "${word.word.lowercase()}-${type.name}-${source.name}",
                word = word,
                type = type,
                prompt = "根据中文写出完整英文单词",
                clue = "${word.meaning} (${word.partOfSpeech})",
                expectedAnswer = word.word,
                source = source
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
                    id = "${word.word.lowercase()}-${type.name}-${source.name}-${start}-${hiddenLength}",
                    word = word,
                    type = type,
                    prompt = "按顺序补全缺少的字母",
                    clue = "$maskedWord\n中文：${word.meaning}",
                    expectedAnswer = missingPart,
                    source = source
                )
            }
        }
    }
}
