package com.xuexi.learningenglish.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xuexi.learningenglish.audio.WordSpeaker
import com.xuexi.learningenglish.data.local.WordStatus
import com.xuexi.learningenglish.ui.screen.DailyLearningScreen
import com.xuexi.learningenglish.ui.screen.DailyPracticeScreen
import com.xuexi.learningenglish.ui.screen.HomeScreen
import com.xuexi.learningenglish.ui.screen.WordDetailScreen
import com.xuexi.learningenglish.ui.screen.WordListScreen
import com.xuexi.learningenglish.ui.screen.WrongBookScreen
import com.xuexi.learningenglish.ui.viewmodel.WordViewModel

@Composable
fun AppNavHost(viewModel: WordViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    var speechRate by rememberSaveable { mutableFloatStateOf(1.0f) }
    val speaker = remember(context) { WordSpeaker(context) }

    DisposableEffect(speaker) {
        onDispose {
            speaker.shutdown()
        }
    }

    val onSpeakWord: (String) -> Unit = { word ->
        speaker.speak(word, speechRate) {
            Toast.makeText(context, "当前单词发音加载失败", Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                wrongCount = uiState.wrongCards.size,
                dailyTarget = uiState.dailyTarget,
                onDailyTargetChange = viewModel::updateDailyTarget,
                onOpenDailyLearning = {
                    viewModel.startDailyLearning(uiState.dailyTarget)
                    navController.navigate("daily")
                },
                onOpenWrongBook = { navController.navigate("wrong-book") },
                onOpenWords = { navController.navigate("words") }
            )
        }
        composable("daily") {
            DailyLearningScreen(
                target = uiState.dailyTarget,
                cards = uiState.dailyCards,
                currentIndex = uiState.currentDailyIndex,
                currentCard = uiState.currentDailyCard,
                speechRate = speechRate,
                onSpeechRateChange = { speechRate = it },
                onSpeakWord = onSpeakWord,
                onBack = {
                    viewModel.clearDailySession()
                    navController.popBackStack()
                },
                onRemembered = viewModel::markDailyRemembered,
                onForgotten = viewModel::markDailyForgotten,
                onFinish = {
                    viewModel.startPracticeSession()
                    navController.navigate("daily-practice")
                }
            )
        }
        composable("daily-practice") {
            DailyPracticeScreen(
                questions = uiState.practiceQuestions,
                currentIndex = uiState.currentPracticeIndex,
                currentQuestion = uiState.currentPracticeQuestion,
                correctCount = uiState.practiceCorrectCount,
                wrongCount = uiState.practiceWrongCount,
                speechRate = speechRate,
                onSpeechRateChange = { speechRate = it },
                onSpeakWord = onSpeakWord,
                onSubmitAnswer = viewModel::recordPracticeAnswer,
                onNextQuestion = viewModel::nextPracticeQuestion,
                onFinish = {
                    viewModel.clearDailySession()
                    navController.popBackStack("home", inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("wrong-book") {
            WrongBookScreen(
                wrongCards = uiState.wrongCards,
                speechRate = speechRate,
                onSpeechRateChange = { speechRate = it },
                onSpeakWord = onSpeakWord,
                onOpenWord = { word ->
                    viewModel.selectWord(word)
                    navController.navigate("word/${word}")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("words") {
            WordListScreen(
                uiState = uiState,
                speechRate = speechRate,
                onSpeechRateChange = { speechRate = it },
                onSpeakWord = onSpeakWord,
                onQueryChange = viewModel::updateQuery,
                onOpenWord = { word ->
                    viewModel.selectWord(word)
                    navController.navigate("word/${word}")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "word/{word}",
            arguments = listOf(navArgument("word") { type = NavType.StringType })
        ) {
            WordDetailScreen(
                card = uiState.selectedCard,
                speechRate = speechRate,
                onSpeechRateChange = { speechRate = it },
                onSpeakWord = onSpeakWord,
                onBack = { navController.popBackStack() },
                onMarkLearning = { viewModel.markStatus(WordStatus.LEARNING) },
                onMarkMastered = { viewModel.markStatus(WordStatus.MASTERED) },
                onMarkWrong = { viewModel.markStatus(WordStatus.WRONG) }
            )
        }
    }
}
