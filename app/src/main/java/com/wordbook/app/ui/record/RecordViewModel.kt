package com.wordbook.app.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.app.WordbookApp
import com.wordbook.app.data.repository.WordRepository
import com.wordbook.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecordViewModel : ViewModel() {

    private val dao = WordbookApp.instance.database.wordDao()
    private val repository = WordRepository(dao)

    private val _addedDates = MutableStateFlow<Set<Long>>(emptySet())
    val addedDates: StateFlow<Set<Long>> = _addedDates

    private val _selectedDayCount = MutableStateFlow(0)
    val selectedDayCount: StateFlow<Int> = _selectedDayCount

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    init {
        loadDates()
    }

    fun loadDates() {
        if (_loaded.value) return
        viewModelScope.launch {
            val dates = repository.getAddedDates()
                .map { DateUtils.startOfDay(it) }
                .toSet()
            _addedDates.value = dates
            _loaded.value = true
        }
    }

    fun selectDay(year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            val start = DateUtils.dayToEpoch(year, month, day)
            val end = start + 24 * 60 * 60 * 1000L
            val count = dao.getAddedCountForDay(start, end)
            _selectedDayCount.value = count
        }
    }
}
