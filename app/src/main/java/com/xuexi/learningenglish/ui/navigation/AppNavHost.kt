package com.xuexi.learningenglish.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.xuexi.learningenglish.ui.screen.PointsScreen
import com.xuexi.learningenglish.ui.screen.RankingScreen
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

    LaunchedEffect(uiState.pointsNotice) {
        uiState.pointsNotice?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumePointsNotice()
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                wrongCount = uiState.wrongCards.size,
                totalWordCount = uiState.totalWordCount,
                learnedWordCount = uiState.learnedWordCount,
                unlearnedWordCount = uiState.unlearnedWordCount,
                continuousLearningDays = uiState.continuousLearningDays,
                pointsBalance = uiState.pointsBalance,
                dailyTarget = uiState.dailyTarget,
                versionName = uiState.versionName,
                onDailyTargetChange = viewModel::updateDailyTarget,
                onOpenDailyLearning = {
                    viewModel.startDailyLearning(uiState.dailyTarget)
                    navController.navigate("daily")
                },
                onOpenDailyPractice = {
                    viewModel.startPracticeSession()
                    navController.navigate("daily-practice")
                },
                onOpenWrongBook = { navController.navigate("wrong-book") },
                onOpenWords = { navController.navigate("words") },
                onOpenPoints = { navController.navigate("points") },
                onOpenRanking = { navController.navigate("ranking") },
                onOpenReviewPractice = {
                    viewModel.startReviewPracticeSession()
                    navController.navigate("review-practice")
                }
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
                onFinish = {
                    viewModel.startPracticeSession()
                    navController.navigate("daily-practice")
                }
            )
        }
        composable("daily-practice") {
            DailyPracticeScreen(
                questions = uiState.practiceQuestions,
                mode = uiState.practiceMode,
                currentIndex = uiState.currentPracticeIndex,
                currentQuestion = uiState.currentPracticeQuestion,
                correctCount = uiState.practiceCorrectCount,
                wrongCount = uiState.practiceWrongCount,
                answerRecords = uiState.practiceAnswers,
                previewVisible = uiState.practicePreviewVisible,
                speechRate = speechRate,
                onSpeechRateChange = { speechRate = it },
                onSpeakWord = onSpeakWord,
                onSubmitAnswer = viewModel::recordPracticeAnswer,
                onNextQuestion = viewModel::nextPracticeQuestion,
                onPreviousQuestion = viewModel::previousPracticeQuestion,
                onDismissPreview = viewModel::dismissPracticePreview,
                onFinish = {
                    viewModel.clearDailySession()
                    navController.popBackStack("home", inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("review-practice") {
            DailyPracticeScreen(
                questions = uiState.practiceQuestions,
                mode = uiState.practiceMode,
                currentIndex = uiState.currentPracticeIndex,
                currentQuestion = uiState.currentPracticeQuestion,
                correctCount = uiState.practiceCorrectCount,
                wrongCount = uiState.practiceWrongCount,
                answerRecords = uiState.practiceAnswers,
                previewVisible = uiState.practicePreviewVisible,
                speechRate = speechRate,
                onSpeechRateChange = { speechRate = it },
                onSpeakWord = onSpeakWord,
                onSubmitAnswer = viewModel::recordPracticeAnswer,
                onNextQuestion = viewModel::nextPracticeQuestion,
                onPreviousQuestion = viewModel::previousPracticeQuestion,
                onDismissPreview = viewModel::dismissPracticePreview,
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
        composable("points") {
            PointsScreen(
                pointsBalance = uiState.pointsBalance,
                transactions = uiState.pointTransactions,
                onRedeemCash = viewModel::redeemCash,
                onRedeemCustom = viewModel::redeemCustom,
                onBack = { navController.popBackStack() }
            )
        }
        composable("ranking") {
            RankingScreen(
                items = uiState.errorRanking,
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
