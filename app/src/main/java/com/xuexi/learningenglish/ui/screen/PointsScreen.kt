package com.xuexi.learningenglish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuexi.learningenglish.data.local.PointTransactionEntity
import com.xuexi.learningenglish.ui.theme.Honey
import com.xuexi.learningenglish.ui.theme.HoneyDeep
import com.xuexi.learningenglish.ui.theme.Ink
import com.xuexi.learningenglish.ui.theme.InkSoft
import com.xuexi.learningenglish.ui.theme.PaperBorder
import com.xuexi.learningenglish.ui.theme.PaperSoft
import com.xuexi.learningenglish.ui.theme.paperBackgroundBrush
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsScreen(
    pointsBalance: Int,
    transactions: List<PointTransactionEntity>,
    onRedeemCash: () -> Unit,
    onRedeemCustom: (Int) -> Unit,
    onBack: () -> Unit
) {
    var dialogVisible by rememberSaveable { mutableStateOf(false) }
    var customCost by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperSoft,
                    titleContentColor = Ink,
                    navigationIconContentColor = Ink
                ),
                title = { Text("积分") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperSoft, RoundedCornerShape(18.dp))
                    .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "当前积分", color = InkSoft, fontSize = 14.sp)
                Text(text = "$pointsBalance 分", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { dialogVisible = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                ) {
                    Text("积分兑换")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperSoft, RoundedCornerShape(18.dp))
                    .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "积分规则", color = Ink, fontWeight = FontWeight.Bold)
                Text(text = "答对 1 个单词 +2 分", color = InkSoft)
                Text(text = "答错 1 个单词 -1 分", color = InkSoft)
                Text(text = "错题本单词第 4 次答对并清除时 +4 分", color = HoneyDeep, fontWeight = FontWeight.SemiBold)
            }

            Text(text = "最近记录", color = Ink, fontWeight = FontWeight.Bold)
            if (transactions.isEmpty()) {
                Text(text = "还没有积分记录", color = InkSoft)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions) { item ->
                        PointRecordRow(item = item)
                    }
                }
            }
        }
    }

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            title = { Text("积分兑换") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExchangeOption(
                        title = "100 积分兑换 10 元",
                        buttonText = "立即兑换",
                        onClick = {
                            onRedeemCash()
                            dialogVisible = false
                        }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "自定义积分兑换, 向父母申请一次兑换", color = Ink)
                        OutlinedTextField(
                            value = customCost,
                            onValueChange = { customCost = it.filter(Char::isDigit) },
                            singleLine = true,
                            label = { Text("输入积分数量") }
                        )
                        Button(
                            onClick = {
                                val cost = customCost.toIntOrNull() ?: 0
                                onRedeemCustom(cost)
                                dialogVisible = false
                                customCost = ""
                            },
                            enabled = customCost.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
                        ) {
                            Text("提交申请")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun ExchangeOption(
    title: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperSoft, RoundedCornerShape(14.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, color = Ink, fontWeight = FontWeight.SemiBold)
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Honey)
        ) {
            Text(buttonText)
        }
    }
}

@Composable
private fun PointRecordRow(item: PointTransactionEntity) {
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperSoft, RoundedCornerShape(16.dp))
            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, color = Ink, fontWeight = FontWeight.SemiBold)
            item.word?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, color = InkSoft, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formatter.format(Date(item.createdAt)), color = InkSoft, fontSize = 12.sp)
        }
        Text(
            text = "${if (item.delta > 0) "+" else ""}${item.delta}",
            color = if (item.delta >= 0) HoneyDeep else Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
