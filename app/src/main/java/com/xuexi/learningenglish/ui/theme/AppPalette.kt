package com.xuexi.learningenglish.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Paper = Color(0xFFF8F1DF)
val PaperSoft = Color(0xFFFFFBF1)
val PaperDeep = Color(0xFFEADDBF)
val PaperBorder = Color(0xFFE2D3AE)
val Ink = Color(0xFF17223B)
val InkSoft = Color(0xFF5C6475)
val Honey = Color(0xFFF6C85F)
val HoneyDeep = Color(0xFFD99A21)
val Teal = Color(0xFF2B9A88)
val Coral = Color(0xFFE96B55)

fun paperBackgroundBrush(): Brush = Brush.verticalGradient(listOf(Paper, Color(0xFFF2E6C9)))
