package com.wordbook.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordbook.app.data.entity.WordEntity

@Composable
fun HomeScreen(
    onStartStudy: (List<WordEntity>) -> Unit,
    onStartReview: (List<WordEntity>) -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val isLoading by vm.isLoading.collectAsState()
    val dueCount by vm.dueCount.collectAsState()

    var showStudyDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var studyCount by remember { mutableStateOf("10") }
    var reviewCount by remember { mutableStateOf("10") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Wordbook",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (dueCount > 0) "$dueCount 个单词待复习" else "准备开始学习吧",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { showStudyDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("学习新词", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showReviewDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                "复习旧词${if (dueCount > 0) " ($dueCount)" else ""}",
                fontSize = 20.sp
            )
        }
    }

    if (showStudyDialog) {
        AlertDialog(
            onDismissRequest = { showStudyDialog = false },
            title = { Text("学习新词") },
            text = {
                Column {
                    Text("请输入本次学习的单词数量：")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = studyCount,
                        onValueChange = { studyCount = it.filter { c -> c.isDigit() } },
                        label = { Text("数量") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showStudyDialog = false
                    val count = studyCount.toIntOrNull() ?: 10
                    vm.getLearningWords(count) { words ->
                        if (words.isNotEmpty()) {
                            onStartStudy(words)
                        }
                    }
                }) {
                    Text("开始")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStudyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("复习旧词") },
            text = {
                Column {
                    Text("请输入本次复习的单词数量：")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewCount,
                        onValueChange = { reviewCount = it.filter { c -> c.isDigit() } },
                        label = { Text("数量") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReviewDialog = false
                    val count = reviewCount.toIntOrNull() ?: 10
                    vm.getReviewWords(count) { words ->
                        if (words.isNotEmpty()) {
                            onStartReview(words)
                        }
                    }
                }) {
                    Text("开始")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
