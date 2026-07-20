package com.wordbook.app.ui.example

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.data.repository.WordRepository
import com.wordbook.app.ui.navigation.jsonToWordIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExampleItem(
    val word: String,
    val sentence: String,
    val translation: String
)

class ExampleViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _examples = MutableStateFlow<List<ExampleItem>>(emptyList())
    val examples: StateFlow<List<ExampleItem>> = _examples

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    fun init(wordIdsJson: String) {
        viewModelScope.launch {
            val ids = jsonToWordIds(wordIdsJson)
            val words = ids.mapNotNull { repository.getWordById(it) }
            val items = mutableListOf<ExampleItem>()
            for (w in words) {
                if (w.sentence1.isNotBlank()) {
                    items.add(ExampleItem(w.word, w.sentence1, w.translation1))
                }
                if (w.sentence2.isNotBlank()) {
                    items.add(ExampleItem(w.word, w.sentence2, w.translation2))
                }
            }
            _examples.value = items.shuffled()
        }
    }

    fun flip() {
        _isFlipped.value = !_isFlipped.value
    }

    fun next() {
        _isFlipped.value = false
        if (_currentIndex.value + 1 < _examples.value.size) {
            _currentIndex.value = _currentIndex.value + 1
        }
    }

    fun current(): ExampleItem? {
        val list = _examples.value
        val idx = _currentIndex.value
        return if (list.isNotEmpty() && idx < list.size) list[idx] else null
    }
}

@Composable
fun ExampleScreen(
    wordIdsJson: String,
    onFinish: () -> Unit,
    exampleViewModel: ExampleViewModel = viewModel()
) {
    LaunchedEffect(wordIdsJson) {
        exampleViewModel.init(wordIdsJson)
    }

    val examples by exampleViewModel.examples.collectAsState()
    val currentIndex by exampleViewModel.currentIndex.collectAsState()
    val isFlipped by exampleViewModel.isFlipped.collectAsState()
    val item = exampleViewModel.current()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (examples.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂无例句",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "例句练习  ${currentIndex + 1}/${examples.size}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (item != null) {
            ExampleCard(
                item = item,
                isFlipped = isFlipped,
                modifier = Modifier.weight(1f),
                onFlip = { exampleViewModel.flip() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val isLast = currentIndex >= examples.size - 1

                Button(
                    onClick = {
                        if (isLast) {
                            onFinish()
                        } else {
                            exampleViewModel.next()
                        }
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp).height(48.dp)
                ) {
                    Text(if (isLast) "结束" else "下一个", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ExampleCard(
    item: ExampleItem,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    onFlip: () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f, label = "flip")

    Card(
        modifier = modifier
            .fillMaxWidth()
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
                ExampleFront(item = item)
            } else {
                ExampleBack(item = item)
            }
        }
    }
}

@Composable
private fun ExampleFront(item: ExampleItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val annotated = buildAnnotatedString {
            val lower = item.sentence.lowercase()
            val wordLower = item.word.lowercase()
            var index = 0

            while (index < lower.length) {
                val found = lower.indexOf(wordLower, index)
                if (found == -1) {
                    append(item.sentence.substring(index))
                    break
                }
                append(item.sentence.substring(index, found))
                withStyle(SpanStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)) {
                    append(item.sentence.substring(found, found + item.word.length))
                }
                index = found + item.word.length
            }
        }

        Text(
            text = annotated,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
    }
}

@Composable
private fun ExampleBack(item: ExampleItem) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val annotated = buildAnnotatedString {
            val lower = item.sentence.lowercase()
            val wordLower = item.word.lowercase()
            var index = 0

            while (index < lower.length) {
                val found = lower.indexOf(wordLower, index)
                if (found == -1) {
                    append(item.sentence.substring(index))
                    break
                }
                append(item.sentence.substring(index, found))
                withStyle(SpanStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)) {
                    append(item.sentence.substring(found, found + item.word.length))
                }
                index = found + item.word.length
            }
        }

        Text(
            text = annotated,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = item.translation,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
