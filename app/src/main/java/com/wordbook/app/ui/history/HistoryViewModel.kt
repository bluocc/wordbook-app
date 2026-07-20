package com.wordbook.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.entity.LearningProgress
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryItem(
    val word: WordEntity,
    val progress: LearningProgress?
)

class HistoryViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _items = MutableStateFlow<List<HistoryItem>>(emptyList())
    val items: StateFlow<List<HistoryItem>> = _items

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val words = repository.getAllPoolWords()
            val progresses = repository.getAllProgress()
            val progressMap = progresses.associateBy { it.wordId }
            _items.value = words.map { w -> HistoryItem(w, progressMap[w.id]) }
        }
    }
}
