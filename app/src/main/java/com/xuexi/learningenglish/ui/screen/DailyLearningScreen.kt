package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.data.model.WordCard
import com.xuexi.learningenglish.ui.component.PhonicsBlockView
import com.xuexi.learningenglish.ui.component.SpeechRateSelector
import com.xuexi.learningenglish.ui.theme.BlueEnd
import com.xuexi.learningenglish.ui.theme.BlueStart
import com.xuexi.learningenglish.ui.theme.Mist

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
    onForgotten: () -> Unit,
    onFinish: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
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
                .background(Brush.verticalGradient(listOf(BlueStart, BlueEnd)))
                .padding(20.dp)
        ) {
            when {
                cards.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "正在准备今天的 $target 个单词...", color = Mist, fontSize = 22.sp)
                    }
                }

                currentCard == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "今日学习完成", color = Mist, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "本轮共学习 ${cards.size} 个单词", color = Mist.copy(alpha = 0.9f))
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onFinish) { Text("开始今日练习") }
                    }
                }

                else -> {
                    Column {
                        Text(
                            text = "第 ${currentIndex + 1} / ${cards.size} 个",
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
                        DailyWordCard(
                            card = currentCard,
                            onSpeakWord = { onSpeakWord(currentCard.word.word) }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onForgotten,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("没记住")
                            }
                            Button(
                                onClick = onRemembered,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("记住了")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyWordCard(card: WordCard, onSpeakWord: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = card.word.phonetic,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = card.word.partOfSpeech, color = Mist.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = card.word.meaning,
                color = Mist,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (card.progress?.wrongCount ?: 0 > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "错词次数：${card.progress?.wrongCount ?: 0}  连续答对：${card.progress?.correctStreak ?: 0}/3",
                    color = Mist.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
