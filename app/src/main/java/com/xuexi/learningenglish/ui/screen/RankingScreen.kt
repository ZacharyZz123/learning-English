package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.data.model.WordErrorRankItem
import com.xuexi.learningenglish.ui.theme.Ink
import com.xuexi.learningenglish.ui.theme.InkSoft
import com.xuexi.learningenglish.ui.theme.Paper
import com.xuexi.learningenglish.ui.theme.PaperBorder
import com.xuexi.learningenglish.ui.theme.paperBackgroundBrush

@Composable
fun RankingScreen(
    items: List<WordErrorRankItem>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paperBackgroundBrush())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Ink
                )
            }
            Text(
                text = "排行榜",
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Paper)
                        .border(1.dp, PaperBorder)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "单词：${item.word}",
                        color = Ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "错误次数：${item.errorCount}",
                        color = InkSoft,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
