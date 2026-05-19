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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.xuexi.learningenglish.ui.viewmodel.WordUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    uiState: WordUiState,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeakWord: (String) -> Unit,
    onQueryChange: (String) -> Unit,
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
                title = { Text("单词库") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(paperBackgroundBrush())
                .padding(16.dp)
        ) {
            WordBankSummary(
                totalWordCount = uiState.totalWordCount
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                label = { Text("搜索单词或中文") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            SpeechRateSelector(
                speechRate = speechRate,
                onSpeechRateChange = onSpeechRateChange
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.loading) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = Ink)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.wordCards) { card ->
                        WordRow(
                            card = card,
                            onClick = { onOpenWord(card.word.word) },
                            onSpeakWord = { onSpeakWord(card.word.word) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordBankSummary(
    totalWordCount: Int
) {
    SummaryCard(
        title = "当前词库数量",
        value = "$totalWordCount 个",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    helper: String? = null
) {
    Column(
        modifier = modifier
            .background(PaperSoft, RoundedCornerShape(16.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title, color = InkSoft, fontSize = 12.sp)
        Text(text = value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        helper?.let {
            Text(text = it, color = InkSoft, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WordRow(card: WordCard, onClick: () -> Unit, onSpeakWord: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperSoft, RoundedCornerShape(18.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                PhonicsBlockView(word = card.word)
            }
            IconButton(onClick = onSpeakWord) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "单词发音", tint = Ink)
            }
            card.progress?.let {
                AssistChip(
                    onClick = {},
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Honey.copy(alpha = 0.22f),
                        labelColor = Ink
                    ),
                    label = { Text(it.status) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = card.word.phonetic, color = Ink, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = card.word.meaning, color = InkSoft)
    }
}
