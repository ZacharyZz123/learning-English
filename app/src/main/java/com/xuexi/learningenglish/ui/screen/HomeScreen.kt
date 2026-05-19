package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.ui.theme.BlueEnd
import com.xuexi.learningenglish.ui.theme.BlueStart
import com.xuexi.learningenglish.ui.theme.Mist

@Composable
fun HomeScreen(
    wrongCount: Int,
    dailyTarget: Int,
    onDailyTargetChange: (Int) -> Unit,
    onOpenDailyLearning: () -> Unit,
    onOpenWrongBook: () -> Unit,
    onOpenWords: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BlueStart, BlueEnd)))
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Learning English",
                color = Mist,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "每日学习后会进入今日练习，错词需要连续 4 天练习答对才移出错题本。",
                color = Mist.copy(alpha = 0.9f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(onClick = onOpenDailyLearning)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "每日学习", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "今天学多少个单词")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(15, 30, 50).forEach { target ->
                            AssistChip(
                                onClick = { onDailyTargetChange(target) },
                                label = { Text("${target}") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "当前选择：$dailyTarget 个", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(onClick = onOpenWrongBook)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "错题本", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "学习没通过的单词会留在这里，并优先混入每日学习。")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "当前错词：$wrongCount", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(onClick = onOpenWords)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "单词库", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "查看完整词库，搜索单词和自然拼读分块。")
                }
            }
        }
    }
}
