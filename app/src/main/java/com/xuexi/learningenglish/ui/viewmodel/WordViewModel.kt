package com.xuexi.learningenglish.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xuexi.learningenglish.BuildConfig
import com.xuexi.learningenglish.data.local.PointTransactionEntity
import com.xuexi.learningenglish.data.model.PracticeAnswerRecord
import com.xuexi.learningenglish.data.model.PracticeQuestion
import com.xuexi.learningenglish.data.model.PracticeQuestionSource
import com.xuexi.learningenglish.data.model.PracticeSessionMode
import com.xuexi.learningenglish.data.local.WordStatus
import com.xuexi.learningenglish.data.model.WordCard
import com.xuexi.learningenglish.data.model.WordErrorRankItem
import com.xuexi.learningenglish.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordUiState(
    val loading: Boolean = true,
    val query: String = "",
    val wordCards: List<WordCard> = emptyList(),
    val wrongCards: List<WordCard> = emptyList(),
    val totalWordCount: Int = 0,
    val learnedWordCount: Int = 0,
    val unlearnedWordCount: Int = 0,
    val continuousLearningDays: Int = 0,
    val pointsBalance: Int = 0,
    val errorRanking: List<WordErrorRankItem> = emptyList(),
    val pointTransactions: List<PointTransactionEntity> = emptyList(),
    val versionCode: Int = BuildConfig.VERSION_CODE,
    val versionName: String = BuildConfig.VERSION_NAME,
    val pointsNotice: String? = null,
    val selectedCard: WordCard? = null,
    val dailyTarget: Int = 15,
    val dailyCards: List<WordCard> = emptyList(),
    val currentDailyIndex: Int = 0,
    val practiceQuestions: List<PracticeQuestion> = emptyList(),
    val practiceMode: PracticeSessionMode = PracticeSessionMode.DAILY,
    val currentPracticeIndex: Int = 0,
    val practiceCorrectCount: Int = 0,
    val practiceWrongCount: Int = 0,
    val practiceAnswers: Map<String, PracticeAnswerRecord> = emptyMap(),
    val practicePreviewVisible: Boolean = true
) {
    val currentDailyCard: WordCard?
        get() = dailyCards.getOrNull(currentDailyIndex)

    val dailyFinished: Boolean
        get() = dailyCards.isNotEmpty() && currentDailyIndex >= dailyCards.size

    val currentPracticeQuestion: PracticeQuestion?
        get() = practiceQuestions.getOrNull(currentPracticeIndex)

    val practiceFinished: Boolean
        get() = practiceQuestions.isNotEmpty() && currentPracticeIndex >= practiceQuestions.size
}

class WordViewModel(
    private val repository: WordRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WordUiState())
    val uiState: StateFlow<WordUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        loadWordCards(query)
    }

    fun loadWordCards(query: String = _uiState.value.query) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val cards = repository.getWordCards(query)
            val totalWordCount = repository.getWordCount()
            val learnedWordCount = repository.getLearnedWordCount()
            val unlearnedWordCount = repository.getUnlearnedWordCount()
            val continuousLearningDays = repository.getContinuousLearningDays()
            _uiState.update {
                it.copy(
                    loading = false,
                    wordCards = cards,
                    totalWordCount = totalWordCount,
                    learnedWordCount = learnedWordCount,
                    unlearnedWordCount = unlearnedWordCount,
                    continuousLearningDays = continuousLearningDays
                )
            }
        }
    }

    fun loadWrongWords() {
        viewModelScope.launch {
            val wrongCards = repository.getWrongWords()
            _uiState.update { it.copy(wrongCards = wrongCards) }
        }
    }

    fun selectWord(word: String) {
        viewModelScope.launch {
            val card = repository.getWordCard(word)
            _uiState.update { it.copy(selectedCard = card) }
        }
    }

    fun updateDailyTarget(target: Int) {
        _uiState.update { it.copy(dailyTarget = target) }
    }

    fun startDailyLearning(target: Int = _uiState.value.dailyTarget) {
        viewModelScope.launch {
            val cards = repository.getDailyLearningSet(target)
            repository.markWordsEnteredDailyLearning(cards.map { it.word })
            _uiState.update {
                it.copy(
                    dailyTarget = target,
                    dailyCards = cards,
                    currentDailyIndex = 0
                )
            }
            loadWordCards()
        }
    }

    fun markDailyRemembered() {
        val current = _uiState.value.currentDailyCard ?: return
        viewModelScope.launch {
            repository.completeDailyLearningWord(current.word)
            advanceDailySession()
        }
    }

    fun clearDailySession() {
        _uiState.update {
            it.copy(
                dailyCards = emptyList(),
                currentDailyIndex = 0,
                practiceQuestions = emptyList(),
                practiceMode = PracticeSessionMode.DAILY,
                currentPracticeIndex = 0,
                practiceCorrectCount = 0,
                practiceWrongCount = 0,
                practiceAnswers = emptyMap(),
                practicePreviewVisible = true
            )
        }
    }

    fun markStatus(status: WordStatus) {
        val selected = _uiState.value.selectedCard ?: return
        viewModelScope.launch {
            repository.updateStatus(selected.word, status)
            selectWord(selected.word.word)
            loadWordCards()
            loadWrongWords()
            loadErrorRanking()
        }
    }

    private fun refreshAll() {
        loadWordCards()
        loadWrongWords()
        loadPoints()
        loadErrorRanking()
    }

    fun loadPoints() {
        viewModelScope.launch {
            val pointsBalance = repository.getPointBalance()
            val pointTransactions = repository.getPointTransactions()
            _uiState.update {
                it.copy(pointsBalance = pointsBalance, pointTransactions = pointTransactions)
            }
        }
    }

    fun loadErrorRanking() {
        viewModelScope.launch {
            val errorRanking = repository.getErrorRanking()
            _uiState.update { it.copy(errorRanking = errorRanking) }
        }
    }

    fun startPracticeSession() {
        viewModelScope.launch {
            val todayCards = if (_uiState.value.dailyCards.isEmpty()) {
                val generatedCards = repository.getDailyLearningSet(_uiState.value.dailyTarget)
                repository.markWordsEnteredDailyLearning(generatedCards.map { it.word })
                generatedCards
            } else {
                _uiState.value.dailyCards
            }
            val questions = repository.buildPracticeQuestions(todayCards)
            _uiState.update {
                it.copy(
                    dailyCards = todayCards,
                    practiceQuestions = questions,
                    practiceMode = PracticeSessionMode.DAILY,
                    currentPracticeIndex = 0,
                    practiceCorrectCount = 0,
                    practiceWrongCount = 0,
                    practiceAnswers = emptyMap(),
                    practicePreviewVisible = true
                )
            }
        }
    }

    fun startReviewPracticeSession() {
        viewModelScope.launch {
            val reviewCards = repository.getReviewPracticeSet(limit = 100)
            val questions = repository.buildPracticeQuestions(
                todayCards = reviewCards,
                mode = PracticeSessionMode.REVIEW
            )
            _uiState.update {
                it.copy(
                    practiceQuestions = questions,
                    practiceMode = PracticeSessionMode.REVIEW,
                    currentPracticeIndex = 0,
                    practiceCorrectCount = 0,
                    practiceWrongCount = 0,
                    practiceAnswers = emptyMap(),
                    practicePreviewVisible = true
                )
            }
        }
    }

    fun dismissPracticePreview() {
        _uiState.update { it.copy(practicePreviewVisible = false) }
    }

    fun recordPracticeAnswer(answer: String, isCorrect: Boolean) {
        val currentQuestion = _uiState.value.currentPracticeQuestion ?: return
        _uiState.update {
            it.copy(
                practiceCorrectCount = it.practiceCorrectCount + if (isCorrect) 1 else 0,
                practiceWrongCount = it.practiceWrongCount + if (isCorrect) 0 else 1,
                practiceAnswers = it.practiceAnswers + (
                    currentQuestion.id to PracticeAnswerRecord(
                        questionId = currentQuestion.id,
                        userAnswer = answer,
                        isCorrect = isCorrect
                    )
                )
            )
        }
        viewModelScope.launch {
            val pointResult = repository.recordPracticeResult(currentQuestion.word, isCorrect)
            if (_uiState.value.practiceMode == PracticeSessionMode.REVIEW) {
                repository.markReviewWordCompleted(currentQuestion.word)
            }
            if (
                _uiState.value.practiceMode == PracticeSessionMode.DAILY &&
                !isCorrect &&
                currentQuestion.source == PracticeQuestionSource.NORMAL
            ) {
                val retryQuestion = repository.buildRetryPracticeQuestion(currentQuestion.word)
                _uiState.update { state ->
                    state.copy(practiceQuestions = state.practiceQuestions + retryQuestion)
                }
            }
            _uiState.update {
                it.copy(
                    pointsBalance = pointResult.balance,
                    pointsNotice = "${pointResult.title} ${if (pointResult.delta > 0) "+" else ""}${pointResult.delta}分"
                )
            }
            loadWrongWords()
            loadWordCards()
            loadPoints()
            loadErrorRanking()
        }
    }

    fun consumePointsNotice() {
        _uiState.update { it.copy(pointsNotice = null) }
    }

    fun redeemCash() {
        viewModelScope.launch {
            val result = repository.redeemCash()
            _uiState.update {
                it.copy(pointsBalance = result.balance, pointsNotice = result.message)
            }
            loadPoints()
        }
    }

    fun redeemCustom(cost: Int) {
        viewModelScope.launch {
            val result = repository.redeemCustom(cost)
            _uiState.update {
                it.copy(pointsBalance = result.balance, pointsNotice = result.message)
            }
            loadPoints()
        }
    }

    fun nextPracticeQuestion() {
        _uiState.update { it.copy(currentPracticeIndex = it.currentPracticeIndex + 1) }
    }

    fun previousPracticeQuestion() {
        _uiState.update {
            it.copy(currentPracticeIndex = (it.currentPracticeIndex - 1).coerceAtLeast(0))
        }
    }

    private suspend fun advanceDailySession() {
        val nextIndex = _uiState.value.currentDailyIndex + 1
        _uiState.update { it.copy(currentDailyIndex = nextIndex) }
        if (nextIndex >= _uiState.value.dailyCards.size && _uiState.value.dailyCards.isNotEmpty()) {
            val continuousLearningDays = repository.markDailyLearningCompleted()
            _uiState.update { it.copy(continuousLearningDays = continuousLearningDays) }
        }
        loadWordCards()
        loadWrongWords()
        loadErrorRanking()
    }
}

class WordViewModelFactory(
    private val repository: WordRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WordViewModel(repository) as T
    }
}
