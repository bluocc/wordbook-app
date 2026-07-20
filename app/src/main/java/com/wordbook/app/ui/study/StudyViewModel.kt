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

class StudyViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _words = MutableStateFlow<List<WordEntity>>(emptyList())
    val words: StateFlow<List<WordEntity>> = _words

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private val _mode = MutableStateFlow("learning")
    val mode: StateFlow<String> = _mode

    private val _currentProgress = MutableStateFlow<LearningProgress?>(null)
    val currentProgress: StateFlow<LearningProgress?> = _currentProgress

    fun init(wordIdsJson: String, mode: String) {
        _mode.value = mode
        viewModelScope.launch {
            val ids = jsonToWordIds(wordIdsJson)
            val wordList = ids.mapNotNull { repository.getWordById(it) }
            _words.value = wordList
        }
    }

    fun currentWord(): WordEntity? {
        val list = _words.value
        val idx = _currentIndex.value
        return if (list.isNotEmpty() && idx < list.size) list[idx] else null
    }

    fun flip() {
        _isFlipped.value = !_isFlipped.value
    }

    fun setFlipped(flipped: Boolean) {
        _isFlipped.value = flipped
    }

    fun rate(quality: Int) {
        val word = currentWord() ?: return
        viewModelScope.launch {
            val prev = repository.getProgress(word.id)
            val now = DateUtils.nowMillis()
            val progress = SM2Scheduler.calculate(quality, now, prev)
            repository.upsertProgress(progress)
            nextCard()
        }
    }

    fun skip() {
        nextCard()
    }

    private fun nextCard() {
        _isFlipped.value = false
        val nextIdx = _currentIndex.value + 1
        if (nextIdx >= _words.value.size) {
            _isFinished.value = true
        } else {
            _currentIndex.value = nextIdx
        }
    }

    fun getAllWords(): List<WordEntity> = _words.value

    fun loadProgress() {
        val word = currentWord() ?: return
        viewModelScope.launch {
            _currentProgress.value = repository.getProgress(word.id)
        }
    }
}
