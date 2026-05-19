package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.ui.theme.Honey
import com.xuexi.learningenglish.ui.theme.HoneyDeep
import com.xuexi.learningenglish.ui.theme.Ink
import com.xuexi.learningenglish.ui.theme.InkSoft
import com.xuexi.learningenglish.ui.theme.Paper
import com.xuexi.learningenglish.ui.theme.PaperBorder
import com.xuexi.learningenglish.ui.theme.PaperDeep
import com.xuexi.learningenglish.ui.theme.PaperSoft
import com.xuexi.learningenglish.ui.theme.Teal
import com.xuexi.learningenglish.ui.theme.paperBackgroundBrush

@Composable
fun HomeScreen(
    wrongCount: Int,
    totalWordCount: Int,
    learnedWordCount: Int,
    unlearnedWordCount: Int,
    continuousLearningDays: Int,
    pointsBalance: Int,
    dailyTarget: Int,
    versionName: String,
    onDailyTargetChange: (Int) -> Unit,
    onOpenDailyLearning: () -> Unit,
    onOpenDailyPractice: () -> Unit,
    onOpenWrongBook: () -> Unit,
    onOpenWords: () -> Unit,
    onOpenPoints: () -> Unit,
    onOpenRanking: () -> Unit,
    onOpenReviewPractice: () -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(paperBackgroundBrush())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderPanel(
                learnedWordCount = learnedWordCount,
                unlearnedWordCount = unlearnedWordCount,
                continuousLearningDays = continuousLearningDays,
                pointsBalance = pointsBalance,
                versionName = versionName,
                onOpenPoints = onOpenPoints,
                onOpenRanking = onOpenRanking
            )
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LearningPanel(
                    dailyTarget = dailyTarget,
                    onDailyTargetChange = onDailyTargetChange,
                    onOpenDailyLearning = onOpenDailyLearning,
                    modifier = Modifier
                        .weight(1.12f)
                        .fillMaxHeight(),
                    compact = true
                )
                PracticePanel(
                    dailyTarget = dailyTarget,
                    wrongCount = wrongCount,
                    onOpenDailyPractice = onOpenDailyPractice,
                    modifier = Modifier
                        .weight(0.88f)
                        .fillMaxHeight(),
                    compact = true
                )
            }
            MiniFeatureCard(
                title = "复习练习",
                subtitle = "从学过的单词里安排复习, 每次最多 100 个",
                footer = "已学过 $learnedWordCount 个",
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                iconTint = HoneyDeep,
                modifier = Modifier.fillMaxWidth(),
                fixedAspect = false,
                onClick = onOpenReviewPractice
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniFeatureCard(
                    title = "错题本",
                    subtitle = "当前 $wrongCount 个错题",
                    footer = "针对性巩固",
                    icon = Icons.Filled.Checklist,
                    iconTint = HoneyDeep,
                    modifier = Modifier.weight(1f),
                    aspectRatioValue = 1.08f,
                    onClick = onOpenWrongBook
                )
                MiniFeatureCard(
                    title = "单词库",
                    subtitle = "",
                    footer = "全部单词 $totalWordCount 个",
                    icon = Icons.Filled.Public,
                    iconTint = Teal,
                    modifier = Modifier.weight(1f),
                    aspectRatioValue = 1.08f,
                    onClick = onOpenWords
                )
            }
        }
    }
}

@Composable
private fun HeaderPanel(
    learnedWordCount: Int,
    unlearnedWordCount: Int,
    continuousLearningDays: Int,
    pointsBalance: Int,
    versionName: String,
    onOpenPoints: () -> Unit,
    onOpenRanking: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperSoft, RoundedCornerShape(22.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(22.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "酸梅汤的英语小屋",
            color = Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            NoteClip()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hi, 同学",
                    color = Ink,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "欢迎来到英语小屋..快来一起学英语!",
                    color = InkSoft,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            StickyAction()
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryChip(
                label = "持续学习",
                value = "$continuousLearningDays 天",
                modifier = Modifier.weight(1f)
            )
            SummaryChip(
                label = "积分",
                value = "$pointsBalance 分",
                modifier = Modifier.weight(1f),
                onClick = onOpenPoints
            )
            CompactEntryChip(
                title = "排行榜",
                modifier = Modifier.weight(1f),
                onClick = onOpenRanking
            )
        }
        Text(
            text = "已学过 $learnedWordCount 个, 剩余未学 $unlearnedWordCount 个",
            color = InkSoft,
            fontSize = 10.sp
        )
        Text(
            text = "版本号: $versionName",
            color = InkSoft,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CompactEntryChip(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 80.dp)
            .background(Paper, RoundedCornerShape(14.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            color = InkSoft,
            fontSize = 11.sp
        )
        Text(
            text = "查看",
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LearningPanel(
    dailyTarget: Int,
    onDailyTargetChange: (Int) -> Unit,
    onOpenDailyLearning: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    FeaturePanel(
        title = "每日学习",
        subtitle = if (compact) "先学新词" else "先学新词, 再去练习巩固",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        iconTint = Teal,
        onClick = onOpenDailyLearning,
        modifier = modifier,
        compact = compact
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 50).forEach { target ->
                CountPill(
                    label = "${target}个",
                    selected = dailyTarget == target,
                    onClick = { onDailyTargetChange(target) },
                    compact = compact,
                    modifier = if (compact) Modifier.weight(1f) else Modifier
                )
            }
        }
        Text(
            text = "当前学习量: $dailyTarget 个",
            color = InkSoft,
            fontSize = if (compact) 12.sp else 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PracticePanel(
    dailyTarget: Int,
    wrongCount: Int,
    onOpenDailyPractice: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    FeaturePanel(
        title = "每日练习",
        subtitle = if (compact) "随时开始练习" else "随时开始练习, 不必等学完再进来",
        icon = Icons.AutoMirrored.Filled.LibraryBooks,
        iconTint = HoneyDeep,
        onClick = onOpenDailyPractice,
        modifier = modifier,
        compact = compact
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "练习进度: 0/${dailyTarget + wrongCount}",
                color = InkSoft,
                fontSize = if (compact) 12.sp else 14.sp
            )
            if (!compact) {
                ProgressTrack(progress = 0f)
            }
        }
    }
}

@Composable
private fun FeaturePanel(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(PaperSoft, RoundedCornerShape(18.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .then(if (compact) Modifier.heightIn(min = 168.dp) else Modifier)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeatureIcon(icon = icon, tint = iconTint)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = InkSoft
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    color = InkSoft,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeatureIcon(icon = icon, tint = iconTint)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                    text = title,
                    color = Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    color = InkSoft,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = InkSoft
                )
            }
        }
        content()
    }
}

@Composable
private fun FeatureIcon(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(tint.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    caption: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .heightIn(min = 80.dp)
            .background(Paper, RoundedCornerShape(14.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = label, color = InkSoft, fontSize = 11.sp)
        Text(text = value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        caption?.let {
            Text(text = it, color = InkSoft, fontSize = 9.sp, lineHeight = 12.sp)
        }
    }
}

@Composable
private fun CountPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val background = if (selected) Honey else Color.White
    val border = if (selected) Honey else PaperBorder
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 0.dp else 22.dp,
                vertical = if (compact) 7.dp else 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Ink,
            fontSize = if (compact) 12.sp else 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProgressTrack(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.34f)
            .height(8.dp)
            .background(PaperDeep, RoundedCornerShape(999.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .background(Honey, RoundedCornerShape(999.dp))
        )
    }
}

@Composable
private fun MiniFeatureCard(
    title: String,
    subtitle: String,
    footer: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    fixedAspect: Boolean = true,
    aspectRatioValue: Float = 0.92f,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .then(if (fixedAspect) Modifier.aspectRatio(aspectRatioValue) else Modifier)
            .heightIn(min = if (fixedAspect) 0.dp else 124.dp)
            .background(PaperSoft, RoundedCornerShape(18.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FeatureIcon(icon = icon, tint = iconTint)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = InkSoft,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(text = title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (subtitle.isNotBlank()) {
            Text(text = subtitle, color = InkSoft, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = footer, color = iconTint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StickyAction() {
    Box(
        modifier = Modifier
            .rotate(5f)
            .background(Honey, RoundedCornerShape(4.dp))
            .border(1.dp, HoneyDeep.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Let's go!",
            color = Ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NoteClip() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .rotate(-18f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AttachFile,
            contentDescription = null,
            tint = HoneyDeep,
            modifier = Modifier.size(24.dp)
        )
    }
}
