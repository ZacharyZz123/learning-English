package com.xuexi.learningenglish.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text

@Composable
fun SpeechRateSelector(
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = speechRate == 0.5f,
            onClick = { onSpeechRateChange(0.5f) },
            label = { Text("0.5x") }
        )
        FilterChip(
            selected = speechRate == 1.0f,
            onClick = { onSpeechRateChange(1.0f) },
            label = { Text("1x") }
        )
    }
}
