package com.trainig.workouttracker.presentation.workout

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.SessionSet
import com.trainig.workouttracker.core.domain.model.WorkoutSession
import com.trainig.workouttracker.core.domain.repository.WorkoutPlanRepository
import com.trainig.workouttracker.core.domain.repository.WorkoutSessionRepository
import com.trainig.workouttracker.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>(Screen.Workout.ARG_SESSION_ID) ?: 0L

    val session: StateFlow<WorkoutSession?> = workoutSessionRepository.observeSession(sessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    data class RestTimer(val totalSeconds: Int, val remainingSeconds: Int, val nextUp: String)

    private val _restTimer = MutableStateFlow<RestTimer?>(null)
    val restTimer: StateFlow<RestTimer?> = _restTimer.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var restJob: Job? = null
    private var restEndsAt = 0L

    fun toggleSet(set: SessionSet) {
        val updated = set.copy(completed = !set.completed)
        viewModelScope.launch { workoutSessionRepository.updateSet(updated) }

        if (updated.completed) {
            val remaining = session.value?.sets?.filter { !it.completed && it.id != set.id }.orEmpty()
            val nextUp = remaining.firstOrNull()
            if (nextUp != null && set.restSeconds > 0) {
                startRest(set.restSeconds, nextUp)
            }
        }
    }

    fun adjustWeight(set: SessionSet, deltaKg: Double) = setWeight(set, set.weightKg + deltaKg)

    fun setWeight(set: SessionSet, weightKg: Double) {
        viewModelScope.launch { workoutSessionRepository.updateSet(set.copy(weightKg = weightKg.coerceAtLeast(0.0))) }
    }

    fun adjustReps(set: SessionSet, delta: Int) = setReps(set, set.reps + delta)

    fun setReps(set: SessionSet, reps: Int) {
        viewModelScope.launch { workoutSessionRepository.updateSet(set.copy(reps = reps.coerceAtLeast(0))) }
    }

    fun addRestTime(seconds: Int) {
        if (_restTimer.value == null) return
        restEndsAt += seconds * 1000L
        _restTimer.update { it?.copy(totalSeconds = it.totalSeconds + seconds) }
    }

    fun skipRest() {
        restJob?.cancel()
        _restTimer.value = null
    }

    private fun startRest(seconds: Int, nextUp: SessionSet) {
        restJob?.cancel()
        restEndsAt = SystemClock.elapsedRealtime() + seconds * 1000L
        val label = "${nextUp.exerciseName} · set ${nextUp.setNumber}"
        _restTimer.value = RestTimer(totalSeconds = seconds, remainingSeconds = seconds, nextUp = label)

        // Count down against the clock rather than summing delays, so the timer can't drift
        restJob = viewModelScope.launch {
            while (true) {
                val remaining = ceil((restEndsAt - SystemClock.elapsedRealtime()) / 1000.0).toInt()
                if (remaining <= 0) break
                _restTimer.update { it?.copy(remainingSeconds = remaining) }
                delay(250)
            }
            _restTimer.value = null
            _uiEvent.emit(UiEvent.RestFinished)
        }
    }

    /**
     * Ends the workout. With [updatePlan], each plan exercise takes the weight and reps
     * of its last completed set, so the next session starts where this one left off.
     */
    fun finish(updatePlan: Boolean) {
        val current = session.value ?: return
        skipRest()
        viewModelScope.launch {
            val planId = current.planId
            if (updatePlan && planId != null) {
                workoutPlanRepository.getPlan(planId)?.let { plan ->
                    val lastDone = current.exercises.associate { sets ->
                        sets.first().exerciseIndex to sets.lastOrNull { it.completed }
                    }
                    val exercises = plan.exercises.mapIndexed { index, exercise ->
                        // Skip if the plan was edited mid-workout and this slot now holds another exercise
                        lastDone[index]
                            ?.takeIf { it.exerciseName == exercise.name }
                            ?.let { exercise.copy(weightKg = it.weightKg, reps = it.reps) }
                            ?: exercise
                    }
                    workoutPlanRepository.savePlan(plan.copy(exercises = exercises))
                }
            }
            workoutSessionRepository.finishSession(current.id)
            _uiEvent.emit(UiEvent.Finished(current.completedSets, current.totalSets, current.volumeKg))
        }
    }

    fun discard() {
        skipRest()
        viewModelScope.launch {
            workoutSessionRepository.discardSession(sessionId)
            _uiEvent.emit(UiEvent.Discarded)
        }
    }

    sealed interface UiEvent {
        data object RestFinished : UiEvent
        data class Finished(val completedSets: Int, val totalSets: Int, val volumeKg: Double) : UiEvent
        data object Discarded : UiEvent
    }
}
