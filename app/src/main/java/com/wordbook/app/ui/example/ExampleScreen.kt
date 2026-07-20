package com.wordbook.app.ui.example

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.data.repository.WordRepository
import com.wordbook.app.ui.navigation.jsonToWordIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ExampleItem(val word: String, val sentence: String, val translation: String)

class ExampleViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _examples = MutableStateFlow<List<ExampleItem>>(emptyList())
    val examples: StateFlow<List<ExampleItem>> = _examples

    private val _currentItem = MutableStateFlow<ExampleItem?>(null)
    val currentItem: StateFlow<ExampleItem?> = _currentItem

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    private var currentIndex = -1
    private var listSize = 0

    fun init(wordIdsJson: String) {
        viewModelScope.launch {
            val ids = jsonToWordIds(wordIdsJson)
            val words = ids.mapNotNull { repository.getWordById(it) }
            val items = mutableListOf<ExampleItem>()
            for (w in words) {
                if (w.sentence1.isNotBlank()) items.add(ExampleItem(w.word, w.sentence1, w.translation1))
                if (w.sentence2.isNotBlank()) items.add(ExampleItem(w.word, w.sentence2, w.translation2))
            }
            _examples.value = items
            listSize = items.size
            if (items.isNotEmpty()) {
                currentIndex = pickRandomIndex(-1)
                _currentItem.value = items[currentIndex]
            }
        }
    }

    fun flip() { _isFlipped.value = !_isFlipped.value }

    fun next() {
        _isFlipped.value = false
        val next = pickRandomIndex(currentIndex)
        if (next in _examples.value.indices) {
            currentIndex = next
            _currentItem.value = _examples.value[next]
        }
    }

    private fun pickRandomIndex(exclude: Int): Int {
        if (listSize <= 0) return 0
        if (listSize == 1) return 0
        var idx: Int
        do { idx = Random.nextInt(listSize) } while (idx == exclude && listSize > 1)
        return idx
    }
}

@Composable
fun ExampleScreen(
    wordIdsJson: String,
    onFinish: () -> Unit,
    vm: ExampleViewModel = viewModel()
) {
    LaunchedEffect(wordIdsJson) { vm.init(wordIdsJson) }
    val examples by vm.examples.collectAsState()
    val item by vm.currentItem.collectAsState()
    val isFlipped by vm.isFlipped.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (examples.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无例句", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            return@Column
        }

        Text(text = "例句练习", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (item != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { vm.flip() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Crossfade(targetState = isFlipped, modifier = Modifier.fillMaxSize()) { flipped ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (!flipped) ExampleFront(item!!) else ExampleBack(item!!)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { vm.next() }, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("下一个")
                }
                OutlinedButton(onClick = onFinish, modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("结束", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ExampleFront(item: ExampleItem) {
    val annotated = buildAnnotatedString {
        val lower = item.sentence.lowercase()
        val wl = item.word.lowercase()
        var i = 0
        while (i < lower.length) {
            val f = lower.indexOf(wl, i)
            if (f == -1) { append(item.sentence.substring(i)); break }
            append(item.sentence.substring(i, f))
            withStyle(SpanStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)) {
                append(item.sentence.substring(f, f + item.word.length))
            }
            i = f + item.word.length
        }
    }
    Text(annotated, fontSize = 24.sp, textAlign = TextAlign.Center, lineHeight = 36.sp, modifier = Modifier.padding(24.dp))
}

@Composable
private fun ExampleBack(item: ExampleItem) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val annotated = buildAnnotatedString {
            val lower = item.sentence.lowercase()
            val wl = item.word.lowercase()
            var i = 0
            while (i < lower.length) {
                val f = lower.indexOf(wl, i)
                if (f == -1) { append(item.sentence.substring(i)); break }
                append(item.sentence.substring(i, f))
                withStyle(SpanStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)) {
                    append(item.sentence.substring(f, f + item.word.length))
                }
                i = f + item.word.length
            }
        }
        Text(annotated, fontSize = 22.sp, textAlign = TextAlign.Center, lineHeight = 34.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.word,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = item.translation,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
    }
}
