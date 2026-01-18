package com.trainig.workouttracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.DailySummary
import com.trainig.workouttracker.core.domain.usecase.GetDailySummaryUseCase
import com.trainig.workouttracker.core.domain.usecase.GetHistoryDatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryDatesUseCase: GetHistoryDatesUseCase,
    private val getDailySummaryUseCase: GetDailySummaryUseCase
) : ViewModel() {

    private val _historyDates = MutableStateFlow<List<LocalDate>>(emptyList())
    val historyDates: StateFlow<List<LocalDate>> = _historyDates.asStateFlow()

    private val _selectedDateSummary = MutableStateFlow<DailySummary?>(null)
    val selectedDateSummary: StateFlow<DailySummary?> = _selectedDateSummary.asStateFlow()

    init {
        loadHistoryDates()
    }

    private fun loadHistoryDates() {
        viewModelScope.launch {
            _historyDates.value = getHistoryDatesUseCase()
        }
    }

    fun loadSummaryForDate(date: LocalDate) {
        viewModelScope.launch {
            _selectedDateSummary.value = getDailySummaryUseCase(date)
        }
    }

    fun clearSelectedDate() {
        _selectedDateSummary.value = null
    }
}

