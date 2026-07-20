package com.wordbook.app.ui.study

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
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
    viewModel: StudyViewModel = viewModel()
) {
    LaunchedEffect(wordIdsJson) {
        viewModel.init(wordIdsJson, mode)
    }

    val words by viewModel.words.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()

    LaunchedEffect(isFinished) {
        if (isFinished) {
            onFinish()
        }
    }

    val word = viewModel.currentWord()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (words.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentIndex + 1} / ${words.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / words.size.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (word != null) {
            FlashCard(
                word = word,
                isFlipped = isFlipped,
                onFlip = {
                    viewModel.flip()
                    viewModel.loadProgress()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RatingButtons(
                onRate = { quality -> viewModel.rate(quality) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        val allWords = viewModel.getAllWords()
                        if (allWords.isNotEmpty()) {
                            onStartExample(allWords)
                        }
                    }
                ) {
                    Text("例句练习")
                }

                OutlinedButton(
                    onClick = { viewModel.skip() }
                ) {
                    Text("下一张")
                }
            }
        }
    }
}

@Composable
fun FlashCard(
    word: WordEntity,
    isFlipped: Boolean,
    onFlip: () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f, label = "flip")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clickable { onFlip() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                FrontCard(word = word)
            } else {
                BackCard(word = word)
            }
        }
    }
}

@Composable
private fun FrontCard(word: WordEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = word.word,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = word.pronunciation,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BackCard(word: WordEntity) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = word.word,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = word.pronunciation,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = word.explanation,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (word.sentence1.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = word.sentence1,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = word.translation1,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        if (word.sentence2.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = word.sentence2,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = word.translation2,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun RatingButtons(onRate: (Int) -> Unit) {
    val labels = listOf("Again", "Hard", "Good", "Easy", "完美")
    val qualities = listOf(1, 2, 3, 4, 5)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        qualities.forEachIndexed { index, quality ->
            val color = qualityColor(quality)
            Button(
                onClick = { onRate(quality) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = labels[index],
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
