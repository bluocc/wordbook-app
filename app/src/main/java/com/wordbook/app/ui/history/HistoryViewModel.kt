package com.wordbook.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.entity.LearningProgress
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.data.repository.WordRepository
import com.wordbook.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryItem(val word: WordEntity, val progress: LearningProgress?)

class HistoryViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _items = MutableStateFlow<List<HistoryItem>>(emptyList())
    val items: StateFlow<List<HistoryItem>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadByDate(start: Long, end: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val words = dao.getPoolWordsByDate(start, end)
            val progresses = dao.getProgressByDate(start, end)
            val map = progresses.associateBy { it.wordId }
            _items.value = words.map { HistoryItem(it, map[it.id]) }
            _isLoading.value = false
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            val words = repository.getAllPoolWords()
            val progresses = repository.getAllProgress()
            val map = progresses.associateBy { it.wordId }
            _items.value = words.map { HistoryItem(it, map[it.id]) }
            _isLoading.value = false
        }
    }
}
