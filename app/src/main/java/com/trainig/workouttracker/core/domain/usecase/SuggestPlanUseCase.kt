package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.Equipment
import com.trainig.workouttracker.core.domain.model.ExperienceLevel
import com.trainig.workouttracker.core.domain.model.PlanExercise
import com.trainig.workouttracker.core.domain.model.PlanPreferences
import com.trainig.workouttracker.core.domain.model.TrainingGoal
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.plan.ExerciseLibrary
import com.trainig.workouttracker.core.domain.plan.LibraryExercise
import javax.inject.Inject
import kotlin.random.Random

/**
 * Offline, rule-based plan generator. Fills the focus' muscle slots with exercises the
 * chosen equipment allows, compound lifts first, then applies sets/reps/rest for the goal.
 */
class SuggestPlanUseCase @Inject constructor() {

    operator fun invoke(preferences: PlanPreferences, random: Random = Random.Default): WorkoutPlan {
        val count = exerciseCount(preferences)
        val chosen = mutableListOf<LibraryExercise>()
        val slots = preferences.focus.slots

        var slotIndex = 0
        var misses = 0
        while (chosen.size < count && misses < slots.size) {
            val muscle = slots[slotIndex % slots.size]
            val candidates = ExerciseLibrary.all.filter {
                it.muscle == muscle && it.isAvailableWith(preferences.equipment) && it !in chosen
            }
            // Lead with compound lifts; save isolation work for the back half of the session
            val preferCompound = chosen.size < (count + 1) / 2
            val pool = candidates.filter { it.compound == preferCompound }.ifEmpty { candidates }

            if (pool.isEmpty()) {
                misses++
            } else {
                chosen += pool.random(random)
                misses = 0
            }
            slotIndex++
        }

        val exercises = chosen
            .sortedByDescending { it.compound }
            .map { it.toPlanExercise(preferences) }

        return WorkoutPlan(
            name = "${preferences.focus.label} · ${preferences.goal.label}",
            exercises = exercises
        )
    }

    private fun exerciseCount(preferences: PlanPreferences): Int {
        var count = when {
            preferences.durationMinutes <= 30 -> 4
            preferences.durationMinutes <= 45 -> 5
            preferences.durationMinutes <= 60 -> 6
            preferences.durationMinutes <= 75 -> 7
            else -> 8
        }
        // Heavy strength work needs long rests, so fit fewer exercises into the same time
        if (preferences.goal == TrainingGoal.STRENGTH) count--
        if (preferences.experience == ExperienceLevel.BEGINNER) count = count.coerceAtMost(5)
        return count.coerceAtLeast(3)
    }

    private fun LibraryExercise.toPlanExercise(preferences: PlanPreferences): PlanExercise {
        val (baseSets, reps, rest) = when (preferences.goal) {
            TrainingGoal.STRENGTH -> if (compound) Triple(5, 5, 180) else Triple(3, 8, 90)
            TrainingGoal.MUSCLE -> if (compound) Triple(4, 8, 120) else Triple(3, 12, 60)
            TrainingGoal.ENDURANCE -> if (compound) Triple(3, 15, 60) else Triple(2, 20, 45)
            TrainingGoal.GENERAL -> if (compound) Triple(3, 10, 90) else Triple(3, 12, 60)
        }
        val sets = when (preferences.experience) {
            ExperienceLevel.BEGINNER -> baseSets - 1
            ExperienceLevel.INTERMEDIATE -> baseSets
            ExperienceLevel.ADVANCED -> if (compound) baseSets + 1 else baseSets
        }.coerceIn(2, 6)

        return PlanExercise(
            name = name,
            sets = sets,
            reps = reps,
            // Starting weights are personal; leave them for the user to fill in
            weightKg = 0.0,
            restSeconds = rest,
            notes = if (requires == Equipment.BODYWEIGHT && cue.isEmpty()) "Bodyweight" else cue
        )
    }
}
