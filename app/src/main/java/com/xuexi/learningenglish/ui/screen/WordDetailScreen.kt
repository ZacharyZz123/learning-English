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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun WordDetailScreen(
    card: WordCard?,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeakWord: (String) -> Unit,
    onBack: () -> Unit,
    onMarkLearning: () -> Unit,
    onMarkMastered: () -> Unit,
    onMarkWrong: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperSoft,
                    titleContentColor = Ink,
                    navigationIconContentColor = Ink
                ),
                title = { Text("单词详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (card == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("正在加载单词...")
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(paperBackgroundBrush())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PaperBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhonicsBlockView(word = card.word)
                        }
                        IconButton(onClick = { onSpeakWord(card.word.word) }) {
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
                    Spacer(modifier = Modifier.height(10.dp))
                    SpeechRateSelector(
                        speechRate = speechRate,
                        onSpeechRateChange = onSpeechRateChange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = card.word.partOfSpeech, color = InkSoft)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = card.word.meaning, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, PaperBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = PaperSoft)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "学习记录", fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "状态：${card.progress?.status ?: "NEW"}", color = InkSoft)
                    Text(text = "复习次数：${card.progress?.reviewCount ?: 0}", color = InkSoft)
                    Text(text = "错词次数：${card.progress?.wrongCount ?: 0}", color = InkSoft)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onMarkLearning,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PaperBorder, contentColor = Ink)
                        ) { Text("继续学") }
                        Button(
                            onClick = onMarkWrong,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7A8B7), contentColor = Color.White)
                        ) { Text("记错了") }
                        Button(
                            onClick = onMarkMastered,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                        ) { Text("记住了") }
                    }
                }
            }
        }
    }
}
