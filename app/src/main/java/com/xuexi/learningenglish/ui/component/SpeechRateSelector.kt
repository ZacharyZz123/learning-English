package com.xuexi.learningenglish.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import com.xuexi.learningenglish.ui.theme.Honey
import com.xuexi.learningenglish.ui.theme.Ink
import com.xuexi.learningenglish.ui.theme.InkSoft
import com.xuexi.learningenglish.ui.theme.PaperSoft

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
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Honey,
                selectedLabelColor = Ink,
                containerColor = PaperSoft,
                labelColor = InkSoft
            ),
            label = { Text("0.5x", fontWeight = FontWeight.SemiBold) }
        )
        FilterChip(
            selected = speechRate == 1.0f,
            onClick = { onSpeechRateChange(1.0f) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Honey,
                selectedLabelColor = Ink,
                containerColor = PaperSoft,
                labelColor = InkSoft
            ),
            label = { Text("1x", fontWeight = FontWeight.SemiBold) }
        )
    }
}
