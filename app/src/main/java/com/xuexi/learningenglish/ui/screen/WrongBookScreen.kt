package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
fun WrongBookScreen(
    wrongCards: List<WordCard>,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeakWord: (String) -> Unit,
    onOpenWord: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperSoft,
                    titleContentColor = Ink,
                    navigationIconContentColor = Ink
                ),
                title = { Text("错题本") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (wrongCards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(paperBackgroundBrush())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "当前没有错词", color = Ink, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "答错的单词会自动进来，并优先混入每日学习。", color = InkSoft)
                Spacer(modifier = Modifier.height(12.dp))
                SpeechRateSelector(
                    speechRate = speechRate,
                    onSpeechRateChange = onSpeechRateChange
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(paperBackgroundBrush())
                    .padding(16.dp)
            ) {
                SpeechRateSelector(
                    speechRate = speechRate,
                    onSpeechRateChange = onSpeechRateChange
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(wrongCards) { card ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PaperSoft, RoundedCornerShape(18.dp))
                                .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
                                .clickable { onOpenWord(card.word.word) }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PhonicsBlockView(word = card.word)
                                }
                                IconButton(onClick = { onSpeakWord(card.word.word) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "单词发音",
                                        tint = Ink
                                    )
                                }
                                Text(
                                    text = "${card.progress?.practiceCorrectDays ?: 0}/4",
                                    color = Honey,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = card.word.phonetic, color = Ink, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = card.word.meaning, color = InkSoft)
                        }
                    }
                }
            }
        }
    }
}
