package com.xuexi.learningenglish.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.xuexi.learningenglish.data.model.Word
import com.xuexi.learningenglish.ui.theme.AffixGreen
import com.xuexi.learningenglish.ui.theme.ConsonantBlue
import com.xuexi.learningenglish.ui.theme.IrregularGold
import com.xuexi.learningenglish.ui.theme.SilentGray
import com.xuexi.learningenglish.ui.theme.VowelRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhonicsBlockView(
    word: Word,
    modifier: Modifier = Modifier
) {
    val soundItems = word.blocks.indices.mapNotNull { index ->
        val sound = word.sounds.getOrNull(index)?.trim().orEmpty()
        if (sound.isBlank()) {
            null
        } else {
            SoundBlock(
                text = simplifyIpa(sound),
                type = word.types.getOrNull(index)
            )
        }
    }

    Column(modifier = modifier) {
        Text(
            text = word.word,
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
        if (soundItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                soundItems.forEach { item ->
                    Text(
                        text = item.text,
                        color = phonicsColor(item.type),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class SoundBlock(
    val text: String,
    val type: String?
)

private fun simplifyIpa(value: String): String {
    val trimmed = value.trim()
    return if (trimmed.startsWith("/") && trimmed.endsWith("/")) {
        trimmed.substring(1, trimmed.length - 1)
    } else {
        trimmed
    }
}

fun phonicsColor(type: String?): Color {
    return when (type) {
        "vowel", "vowel_team", "r_controlled" -> VowelRed
        "suffix", "prefix" -> AffixGreen
        "silent" -> SilentGray
        "irregular" -> IrregularGold
        else -> ConsonantBlue
    }
}
