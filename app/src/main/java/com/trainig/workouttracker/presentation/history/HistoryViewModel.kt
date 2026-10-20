package com.trainig.workouttracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.DailySummary
import com.trainig.workouttracker.core.domain.model.ProgressStats
import com.trainig.workouttracker.core.domain.usecase.GetDailySummaryUseCase
import com.trainig.workouttracker.core.domain.usecase.GetHistoryDatesUseCase
import com.trainig.workouttracker.core.domain.usecase.GetProgressStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryDatesUseCase: GetHistoryDatesUseCase,
    private val getDailySummaryUseCase: GetDailySummaryUseCase,
    private val getProgressStatsUseCase: GetProgressStatsUseCase
) : ViewModel() {

    enum class StatsRange(val days: Int, val label: String) {
        WEEK(7, "7 days"),
        TWO_WEEKS(14, "14 days"),
        MONTH(30, "30 days")
    }

    private val _historyDates = MutableStateFlow<List<LocalDate>>(emptyList())
    val historyDates: StateFlow<List<LocalDate>> = _historyDates.asStateFlow()

    private val _selectedDateSummary = MutableStateFlow<DailySummary?>(null)
    val selectedDateSummary: StateFlow<DailySummary?> = _selectedDateSummary.asStateFlow()

    private val _statsRange = MutableStateFlow(StatsRange.TWO_WEEKS)
    val statsRange: StateFlow<StatsRange> = _statsRange.asStateFlow()

    /** Exercise the chart is filtered to; null shows all exercises together. */
    private val _statsExerciseId = MutableStateFlow<Long?>(null)
    val statsExerciseId: StateFlow<Long?> = _statsExerciseId.asStateFlow()

    private val _stats = MutableStateFlow<ProgressStats?>(null)
    val stats: StateFlow<ProgressStats?> = _stats.asStateFlow()

    init {
        loadHistoryDates()
        loadStats()
    }

    private fun loadHistoryDates() {
        viewModelScope.launch {
            _historyDates.value = getHistoryDatesUseCase()
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val stats = getProgressStatsUseCase(_statsRange.value.days)
            // Drop the filter if that exercise has nothing logged in the new range
            if (stats.exercises.none { it.id == _statsExerciseId.value }) _statsExerciseId.value = null
            _stats.value = stats
        }
    }

    fun setStatsRange(range: StatsRange) {
        if (range == _statsRange.value) return
        _statsRange.value = range
        loadStats()
    }

    fun setStatsExercise(exerciseId: Long?) {
        _statsExerciseId.value = exerciseId
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
