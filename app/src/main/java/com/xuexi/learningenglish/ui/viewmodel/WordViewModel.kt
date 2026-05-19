package com.xuexi.learningenglish.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xuexi.learningenglish.data.model.PracticeQuestion
import com.xuexi.learningenglish.data.local.WordStatus
import com.xuexi.learningenglish.data.model.WordCard
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
    val selectedCard: WordCard? = null,
    val dailyTarget: Int = 15,
    val dailyCards: List<WordCard> = emptyList(),
    val currentDailyIndex: Int = 0,
    val practiceQuestions: List<PracticeQuestion> = emptyList(),
    val currentPracticeIndex: Int = 0,
    val practiceCorrectCount: Int = 0,
    val practiceWrongCount: Int = 0
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
            _uiState.update { it.copy(loading = false, wordCards = cards) }
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
            _uiState.update {
                it.copy(
                    dailyTarget = target,
                    dailyCards = cards,
                    currentDailyIndex = 0
                )
            }
        }
    }

    fun markDailyRemembered() {
        val current = _uiState.value.currentDailyCard ?: return
        viewModelScope.launch {
            repository.markRemembered(current.word)
            advanceDailySession()
        }
    }

    fun markDailyForgotten() {
        val current = _uiState.value.currentDailyCard ?: return
        viewModelScope.launch {
            repository.markForgotten(current.word)
            advanceDailySession()
        }
    }

    fun clearDailySession() {
        _uiState.update {
            it.copy(
                dailyCards = emptyList(),
                currentDailyIndex = 0,
                practiceQuestions = emptyList(),
                currentPracticeIndex = 0,
                practiceCorrectCount = 0,
                practiceWrongCount = 0
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
        }
    }

    private fun refreshAll() {
        loadWordCards()
        loadWrongWords()
    }

    fun startPracticeSession() {
        val todayCards = _uiState.value.dailyCards
        if (todayCards.isEmpty()) return
        viewModelScope.launch {
            val questions = repository.buildPracticeQuestions(todayCards)
            _uiState.update {
                it.copy(
                    practiceQuestions = questions,
                    currentPracticeIndex = 0,
                    practiceCorrectCount = 0,
                    practiceWrongCount = 0
                )
            }
        }
    }

    fun recordPracticeAnswer(isCorrect: Boolean) {
        val currentQuestion = _uiState.value.currentPracticeQuestion ?: return
        _uiState.update {
            it.copy(
                practiceCorrectCount = it.practiceCorrectCount + if (isCorrect) 1 else 0,
                practiceWrongCount = it.practiceWrongCount + if (isCorrect) 0 else 1
            )
        }
        viewModelScope.launch {
            repository.recordPracticeResult(currentQuestion.word, isCorrect)
            loadWrongWords()
            loadWordCards()
        }
    }

    fun nextPracticeQuestion() {
        _uiState.update { it.copy(currentPracticeIndex = it.currentPracticeIndex + 1) }
    }

    private suspend fun advanceDailySession() {
        _uiState.update { it.copy(currentDailyIndex = it.currentDailyIndex + 1) }
        loadWordCards()
        loadWrongWords()
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
