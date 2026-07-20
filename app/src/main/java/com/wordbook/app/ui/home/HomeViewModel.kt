package com.wordbook.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.data.repository.WordRepository
import com.wordbook.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _dueCount = MutableStateFlow(0)
    val dueCount: StateFlow<Int> = _dueCount

    init {
        viewModelScope.launch {
            repository.loadWordData(WordbookApp.instance)
            val now = DateUtils.nowMillis()
            _dueCount.value = dao.getDueWords(now, 9999).size
            _isLoading.value = false
        }
    }

    fun getLearningWords(count: Int, onResult: (List<WordEntity>) -> Unit) {
        viewModelScope.launch {
            val words = repository.getUnlearnedWords(count)
            onResult(words)
        }
    }

    fun getReviewWords(count: Int, onResult: (List<WordEntity>) -> Unit) {
        viewModelScope.launch {
            val now = DateUtils.nowMillis()
            val words = repository.getDueWords(now, count)
            onResult(words)
        }
    }
}
