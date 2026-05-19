package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.data.model.WordCard
import com.xuexi.learningenglish.ui.component.PhonicsBlockView
import com.xuexi.learningenglish.ui.component.SpeechRateSelector
import com.xuexi.learningenglish.ui.theme.Honey
import com.xuexi.learningenglish.ui.theme.Ink
import com.xuexi.learningenglish.ui.theme.InkSoft
import com.xuexi.learningenglish.ui.theme.PaperBorder
import com.xuexi.learningenglish.ui.theme.PaperSoft
import com.xuexi.learningenglish.ui.theme.paperBackgroundBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLearningScreen(
    target: Int,
    cards: List<WordCard>,
    currentIndex: Int,
    currentCard: WordCard?,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeakWord: (String) -> Unit,
    onBack: () -> Unit,
    onRemembered: () -> Unit,
    onFinish: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperSoft,
                    titleContentColor = Ink,
                    navigationIconContentColor = Ink
                ),
                title = { Text("每日学习") },
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
                cards.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "正在准备今天的 $target 个单词...", color = InkSoft, fontSize = 18.sp)
                    }
                }

                currentCard == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "今日学习完成", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "本轮共学习 ${cards.size} 个单词", color = InkSoft)
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                        ) { Text("开始今日练习") }
                    }
                }

                else -> {
                    val currentWord = currentCard.word.word
                    var quizMode by rememberSaveable(currentWord) { mutableStateOf(false) }
                    var answer by rememberSaveable(currentWord) { mutableStateOf("") }
                    var checked by rememberSaveable(currentWord) { mutableStateOf(false) }
                    var isCorrect by rememberSaveable(currentWord) { mutableStateOf(false) }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "第 ${currentIndex + 1} / ${cards.size} 个",
                            color = Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        SpeechRateSelector(
                            speechRate = speechRate,
                            onSpeechRateChange = onSpeechRateChange
                        )
                        DailyWordCard(
                            card = currentCard,
                            quizMode = quizMode,
                            checked = checked,
                            isCorrect = isCorrect,
                            onSpeakWord = { onSpeakWord(currentCard.word.word) }
                        )
                        if (quizMode) {
                            OutlinedTextField(
                                value = answer,
                                onValueChange = {
                                    answer = it
                                    if (checked) {
                                        checked = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !checked,
                                singleLine = true,
                                label = { Text("请输入英文单词") }
                            )
                        }
                        if (!quizMode) {
                            Button(
                                onClick = {
                                    quizMode = true
                                    answer = ""
                                    checked = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Ink,
                                    contentColor = Honey
                                )
                            ) {
                                Text("记住了")
                            }
                        } else if (!checked) {
                            Button(
                                onClick = {
                                    val normalizedInput = answer.trim()
                                    checked = true
                                    isCorrect = normalizedInput.equals(currentWord, ignoreCase = true)
                                },
                                enabled = answer.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Ink,
                                    contentColor = Honey
                                )
                            ) {
                                Text("提交答案")
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        answer = ""
                                        checked = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD7A8B7),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("重试")
                                }
                                Button(
                                    onClick = onRemembered,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Ink,
                                        contentColor = Honey
                                    )
                                ) {
                                    Text("下一个")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyWordCard(
    card: WordCard,
    quizMode: Boolean,
    checked: Boolean,
    isCorrect: Boolean,
    onSpeakWord: () -> Unit
) {
    val cardRadius: Dp = 18.dp
    Card(
        colors = CardDefaults.cardColors(containerColor = PaperSoft),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PaperBorder, RoundedCornerShape(cardRadius)),
        shape = RoundedCornerShape(cardRadius)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!quizMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhonicsBlockView(word = card.word)
                    }
                    IconButton(onClick = onSpeakWord) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "单词发音",
                            tint = Ink
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = card.word.phonetic,
                    color = Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = card.word.partOfSpeech, color = InkSoft)
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HiddenWordBanner(
                        letterCount = card.word.word.length,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onSpeakWord) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "单词发音",
                            tint = Ink
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = card.word.phonetic,
                    color = Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = card.word.partOfSpeech, color = InkSoft)
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = card.word.meaning,
                color = Ink,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (quizMode) {
                if (checked) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val resultText = if (isCorrect) "答对了" else "答错了，正确答案：${card.word.word}"
                    val resultColor = if (isCorrect) Color(0xFF2F8F5B) else Color(0xFFB35A38)
                    Text(
                        text = resultText,
                        color = resultColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (card.progress?.wrongCount ?: 0 > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "错词次数：${card.progress?.wrongCount ?: 0}  连续答对：${card.progress?.correctStreak ?: 0}/3",
                    color = InkSoft,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HiddenWordBanner(
    letterCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(letterCount.coerceAtMost(12)) { index ->
            val tint = when (index % 3) {
                0 -> Color(0xFFBFD9F5)
                1 -> Color(0xFFF3B3C1)
                else -> Color(0xFFCBEBC9)
            }
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(38.dp)
                    .background(tint.copy(alpha = 0.75f), RoundedCornerShape(3.dp))
            )
        }
    }
}
