package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.data.model.PracticeQuestion
import com.xuexi.learningenglish.ui.component.SpeechRateSelector
import com.xuexi.learningenglish.ui.theme.BlueEnd
import com.xuexi.learningenglish.ui.theme.BlueStart
import com.xuexi.learningenglish.ui.theme.Mist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPracticeScreen(
    questions: List<PracticeQuestion>,
    currentIndex: Int,
    currentQuestion: PracticeQuestion?,
    correctCount: Int,
    wrongCount: Int,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeakWord: (String) -> Unit,
    onSubmitAnswer: (Boolean) -> Unit,
    onNextQuestion: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var answer by rememberSaveable(currentIndex) { mutableStateOf("") }
    var submitted by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var isCorrect by rememberSaveable(currentIndex) { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        answer = ""
        submitted = false
        isCorrect = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日练习") },
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
                .background(Brush.verticalGradient(listOf(BlueStart, BlueEnd)))
                .padding(20.dp)
        ) {
            when {
                questions.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "正在准备今日练习...", color = Mist, fontSize = 22.sp)
                    }
                }

                currentQuestion == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "今日练习完成", color = Mist, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "答对 $correctCount 题，答错 $wrongCount 题", color = Mist.copy(alpha = 0.9f))
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onFinish) { Text("返回首页") }
                    }
                }

                else -> {
                    Column {
                        Text(
                            text = "第 ${currentIndex + 1} / ${questions.size} 题",
                            color = Mist,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SpeechRateSelector(
                            speechRate = speechRate,
                            onSpeechRateChange = onSpeechRateChange
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(text = currentQuestion.prompt, color = Mist, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = currentQuestion.clue,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                IconButton(onClick = { onSpeakWord(currentQuestion.word.word) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "单词发音",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answer = it },
                            enabled = !submitted,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("输入答案") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (submitted) {
                            Text(
                                text = if (isCorrect) {
                                    "回答正确"
                                } else {
                                    "回答错误，正确答案：${currentQuestion.expectedAnswer}"
                                },
                                color = if (isCorrect) Mist else Color(0xFFFFE082),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (currentIndex == questions.lastIndex) {
                                        onNextQuestion()
                                    } else {
                                        onNextQuestion()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (currentIndex == questions.lastIndex) "完成练习" else "下一题")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val result = currentQuestion.isCorrect(answer)
                                    isCorrect = result
                                    submitted = true
                                    onSubmitAnswer(result)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = answer.isNotBlank()
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
