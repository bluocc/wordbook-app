package com.wordbook.app.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.entity.LearningProgress
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.data.repository.WordRepository
import com.wordbook.app.review.SM2Scheduler
import com.wordbook.app.ui.theme.qualityColor
import com.wordbook.app.ui.theme.Grey
import com.wordbook.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _word = MutableStateFlow<WordEntity?>(null)
    val word: StateFlow<WordEntity?> = _word

    private val _progress = MutableStateFlow<LearningProgress?>(null)
    val progress: StateFlow<LearningProgress?> = _progress

    fun load(wordId: Long) {
        viewModelScope.launch {
            _word.value = repository.getWordById(wordId)
            _progress.value = repository.getProgress(wordId)
        }
    }

    fun updateQuality(wordId: Long, quality: Int) {
        viewModelScope.launch {
            val prev = repository.getProgress(wordId)
            val now = DateUtils.nowMillis()
            val p = SM2Scheduler.calculate(quality, now, wordId, prev)
            repository.upsertProgress(p)
            _progress.value = p
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    wordId: Long,
    onBack: () -> Unit,
    vm: DetailViewModel = viewModel()
) {
    LaunchedEffect(wordId) { vm.load(wordId) }

    val word by vm.word.collectAsState()
    val progress by vm.progress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单词详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (word == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val w = word!!
        val currentQuality = progress?.lastQuality ?: 0

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(w.word, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(w.pronunciation, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
            Text(w.explanation, fontSize = 22.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)

            if (w.sentence1.isNotBlank()) {
                Spacer(modifier = Modifier.height(28.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(w.sentence1, fontSize = 17.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(w.translation1, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), textAlign = TextAlign.Center)
                    }
                }
            }

            if (w.sentence2.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(w.sentence2, fontSize = 17.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(w.translation2, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (currentQuality > 0) "当前状态" else "尚未评分",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            RatingRow(selected = currentQuality) { q -> vm.updateQuality(wordId, q) }

            if (progress != null && progress!!.lastReview > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "已复习 ${progress!!.repetitions} 次 · 间隔 ${progress!!.interval} 天",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun RatingRow(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("完全忘了", "有点难", "还行", "简单", "太简单")
    val qualities = listOf(1, 2, 3, 4, 5)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        qualities.forEachIndexed { i, q ->
            val color = qualityColor(q)
            val isSelected = selected == q
            Button(
                onClick = { onSelect(q) },
                modifier = Modifier.weight(1f).padding(horizontal = 3.dp).height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) color else color.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(labels[i], fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
