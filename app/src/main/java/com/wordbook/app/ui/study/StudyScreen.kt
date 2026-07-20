package com.wordbook.app.ui.study

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.ui.theme.qualityColor

@Composable
fun StudyScreen(
    mode: String,
    wordIdsJson: String,
    onStartExample: (List<WordEntity>) -> Unit,
    onFinish: () -> Unit,
    vm: StudyViewModel = viewModel()
) {
    LaunchedEffect(wordIdsJson) { vm.init(wordIdsJson, mode) }

    val words by vm.words.collectAsState()
    val currentWord by vm.currentWord.collectAsState()
    val isFlipped by vm.isFlipped.collectAsState()
    val totalRatings by vm.totalRatings.collectAsState()
    val word = currentWord

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (words.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (mode == "learning") "学习模式" else "复习模式",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "已评 $totalRatings 次",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (word != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { vm.flip() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Crossfade(
                    targetState = isFlipped,
                    modifier = Modifier.fillMaxSize()
                ) { flipped ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!flipped) FrontCard(word) else BackCard(word)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            RatingButtons(onRate = { vm.rate(it) })

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { vm.skip() }) { Text("下一个") }
                OutlinedButton(onClick = { onStartExample(vm.getAllWords()) }) {
                    Text("例句练习")
                }
                OutlinedButton(onClick = onFinish) {
                    Text("结束", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FrontCard(word: WordEntity) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(word.word, fontSize = 42.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(10.dp))
        Text(word.pronunciation, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
    }
}

@Composable
private fun BackCard(word: WordEntity) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(word.word, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(word.pronunciation, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(14.dp))
        Text(word.explanation, fontSize = 22.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        if (word.sentence1.isNotBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(word.sentence1, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(word.translation1, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        if (word.sentence2.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(word.sentence2, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(word.translation2, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun RatingButtons(onRate: (Int) -> Unit) {
    val labels = listOf("完全忘了", "有点难", "还行", "简单", "太简单")
    val qualities = listOf(1, 2, 3, 4, 5)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        qualities.forEachIndexed { i, q ->
            Button(
                onClick = { onRate(q) },
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = qualityColor(q)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(labels[i], fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
