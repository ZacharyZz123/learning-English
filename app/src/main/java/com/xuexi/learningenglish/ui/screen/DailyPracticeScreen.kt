package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.data.model.PracticeAnswerRecord
import com.xuexi.learningenglish.data.model.PracticeQuestion
import com.xuexi.learningenglish.data.model.PracticeQuestionSource
import com.xuexi.learningenglish.data.model.PracticeSessionMode
import com.xuexi.learningenglish.ui.component.SpeechRateSelector
import com.xuexi.learningenglish.ui.theme.Honey
import com.xuexi.learningenglish.ui.theme.Ink
import com.xuexi.learningenglish.ui.theme.InkSoft
import com.xuexi.learningenglish.ui.theme.PaperBorder
import com.xuexi.learningenglish.ui.theme.PaperSoft
import com.xuexi.learningenglish.ui.theme.paperBackgroundBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPracticeScreen(
    questions: List<PracticeQuestion>,
    mode: PracticeSessionMode,
    currentIndex: Int,
    currentQuestion: PracticeQuestion?,
    correctCount: Int,
    wrongCount: Int,
    answerRecords: Map<String, PracticeAnswerRecord>,
    previewVisible: Boolean,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeakWord: (String) -> Unit,
    onSubmitAnswer: (String, Boolean) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onDismissPreview: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var answer by rememberSaveable(currentIndex) { mutableStateOf("") }
    var submitted by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var isCorrect by rememberSaveable(currentIndex) { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        val record = currentQuestion?.let { answerRecords[it.id] }
        answer = record?.userAnswer.orEmpty()
        submitted = record != null
        isCorrect = record?.isCorrect == true
    }

    val screenTitle = if (mode == PracticeSessionMode.REVIEW) "复习练习" else "今日练习"

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperSoft,
                    titleContentColor = Ink,
                    navigationIconContentColor = Ink
                ),
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(paperBackgroundBrush())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                questions.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "正在准备今日练习...", color = InkSoft, fontSize = 18.sp)
                    }
                }

                previewVisible -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = screenTitle, color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (mode == PracticeSessionMode.REVIEW) {
                                "本次最多复习 ${questions.size} 题，优先安排复习次数更少的已学单词。"
                            } else {
                                "本次共 ${questions.size} 题，包含今天学习内容和错题本复习。"
                            },
                            color = InkSoft,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onDismissPreview,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                        ) {
                            Text("开始答题")
                        }
                    }
                }

                currentQuestion == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (mode == PracticeSessionMode.REVIEW) "复习练习完成" else "今日练习完成",
                            color = Ink,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "答对 $correctCount 题，答错 $wrongCount 题", color = InkSoft)
                        val wrongWords = questions
                            .filter { answerRecords[it.id]?.isCorrect == false }
                            .map { it.word.word }
                            .distinct()
                        if (wrongWords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(text = "本次错词", color = Ink, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = wrongWords.joinToString("、"), color = InkSoft)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                        ) { Text("返回首页") }
                    }
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "第 ${currentIndex + 1} / ${questions.size} 题",
                            color = Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        SpeechRateSelector(
                            speechRate = speechRate,
                            onSpeechRateChange = onSpeechRateChange
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PaperSoft),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, PaperBorder, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (currentQuestion.source == PracticeQuestionSource.WRONG_RETRY) {
                                        "错题再练"
                                    } else {
                                        currentQuestion.prompt
                                    },
                                    color = InkSoft,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentQuestion.clue,
                                    color = Ink,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                IconButton(onClick = { onSpeakWord(currentQuestion.word.word) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "单词发音",
                                        tint = Ink
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answer = it },
                            enabled = !submitted,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("输入答案") },
                            singleLine = true
                        )
                        if (submitted) {
                            Text(
                                text = if (isCorrect) {
                                    "回答正确"
                                } else {
                                    "回答错误，正确答案：${currentQuestion.expectedAnswer}"
                                },
                                color = if (isCorrect) Ink else Color(0xFFB36827),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = onPreviousQuestion,
                                    modifier = Modifier.weight(1f),
                                    enabled = currentIndex > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = PaperSoft, contentColor = Ink)
                                ) {
                                    Text("上一题")
                                }
                                Button(
                                    onClick = onNextQuestion,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                                ) {
                                    Text(if (currentIndex == questions.lastIndex) "完成练习" else "下一题")
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val result = currentQuestion.isCorrect(answer)
                                    isCorrect = result
                                    submitted = true
                                    onSubmitAnswer(answer, result)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = answer.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                            ) {
                                Text("提交答案")
                            }
                        }
                    }
                }
            }
        }
    }
}
