package com.trainig.workouttracker.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.WorkoutStats
import com.trainig.workouttracker.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WorkoutStatsViewModel @Inject constructor(
    workoutSessionRepository: WorkoutSessionRepository
) : ViewModel() {

    /** Null until the first load finishes, so the screen doesn't flash its empty state. */
    val stats: StateFlow<WorkoutStats?> = workoutSessionRepository.getFinishedSessions()
        .map { WorkoutStats.from(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _selectedExercise = MutableStateFlow<String?>(null)

    /** Exercise shown in the progress chart; defaults to the most frequently done one. */
    val selectedExercise: StateFlow<String?> = combine(stats, _selectedExercise) { stats, selected ->
        val names = stats?.exercises?.map { it.name }.orEmpty()
        selected?.takeIf { it in names } ?: names.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun selectExercise(name: String) {
        _selectedExercise.value = name
    }
}
