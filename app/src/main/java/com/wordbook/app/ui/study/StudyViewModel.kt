package com.wordbook.app.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.entity.LearningProgress
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.data.repository.WordRepository
import com.wordbook.app.review.SM2Scheduler
import com.wordbook.app.ui.navigation.jsonToWordIds
import com.wordbook.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

class StudyViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _words = MutableStateFlow<List<WordEntity>>(emptyList())
    val words: StateFlow<List<WordEntity>> = _words

    private val _currentWord = MutableStateFlow<WordEntity?>(null)
    val currentWord: StateFlow<WordEntity?> = _currentWord

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    private val _mode = MutableStateFlow("learning")
    val mode: StateFlow<String> = _mode

    private val _totalRatings = MutableStateFlow(0)
    val totalRatings: StateFlow<Int> = _totalRatings

    private var currentIndex = -1
    private var progressCache = mutableMapOf<Long, LearningProgress?>()
    private var poolSize = 0

    fun init(wordIdsJson: String, mode: String) {
        _mode.value = mode
        viewModelScope.launch {
            val ids = jsonToWordIds(wordIdsJson)
            val list = ids.mapNotNull { repository.getWordById(it) }
            _words.value = list
            poolSize = list.size

            val now = DateUtils.nowMillis()
            for (w in list) {
                var prev = repository.getProgress(w.id)
                if (prev == null) {
                    prev = LearningProgress(wordId = w.id, addedToPool = now)
                    repository.upsertProgress(prev)
                }
                progressCache[w.id] = prev
            }

            if (list.isNotEmpty()) {
                currentIndex = pickRandomIndex(-1)
                _currentWord.value = list[currentIndex]
            }
        }
    }

    fun flip() {
        _isFlipped.value = !_isFlipped.value
    }

    fun rate(quality: Int) {
        val word = _currentWord.value ?: return
        viewModelScope.launch {
            val now = DateUtils.nowMillis()
            val prev = progressCache[word.id] ?: repository.getProgress(word.id)
            val progress = SM2Scheduler.calculate(quality, now, word.id, prev)
            repository.upsertProgress(progress)
            progressCache[word.id] = progress
            _totalRatings.value++

            _isFlipped.value = false
            nextWord()
        }
    }

    fun skip() {
        _isFlipped.value = false
        nextWord()
    }

    private fun nextWord() {
        val list = _words.value
        if (list.isEmpty() || poolSize == 0) return

        val nextIdx = pickRandomIndex(currentIndex)
        if (nextIdx in list.indices) {
            currentIndex = nextIdx
            _currentWord.value = list[nextIdx]
        }
    }

    private fun pickRandomIndex(excludeIndex: Int): Int {
        val list = _words.value
        if (list.isEmpty()) return 0
        if (poolSize == 1) return 0

        val weights = list.mapIndexed { i, w ->
            if (i == excludeIndex) 0f
            else {
                val p = progressCache[w.id]
                if (p == null || p.lastQuality == 0) 6f
                else (7f - p.lastQuality.coerceIn(1, 5)).coerceAtLeast(0.5f)
            }
        }

        val totalWeight = weights.sum()
        if (totalWeight <= 0f) {
            return (list.indices).firstOrNull { it != excludeIndex } ?: 0
        }

        val r = Random.nextFloat() * totalWeight
        var cumulative = 0f
        for (i in weights.indices) {
            if (i == excludeIndex) continue
            cumulative += weights[i]
            if (r <= cumulative) return i
        }
        return list.indices.lastOrNull { it != excludeIndex } ?: 0
    }

    fun getAllWords(): List<WordEntity> = _words.value

    fun getProgressFor(wordId: Long): LearningProgress? = progressCache[wordId]

    fun isLastSession(): Boolean = _totalRatings.value >= 5
}
