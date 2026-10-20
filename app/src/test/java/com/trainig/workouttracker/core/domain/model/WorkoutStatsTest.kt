package com.trainig.workouttracker.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutStatsTest {

    private fun set(name: String, weight: Double, reps: Int, completed: Boolean = true) = SessionSet(
        exerciseIndex = 0,
        exerciseName = name,
        setNumber = 1,
        targetReps = reps,
        reps = reps,
        weightKg = weight,
        restSeconds = 60,
        completed = completed
    )

    private fun session(id: Long, startedAt: Long, vararg sets: SessionSet) = WorkoutSession(
        id = id,
        planId = null,
        planName = "Plan",
        startedAt = startedAt,
        finishedAt = startedAt + 1,
        sets = sets.toList()
    )

    @Test
    fun `first time doing an exercise is not a record but beating it is`() {
        val stats = WorkoutStats.from(
            listOf(
                session(1, 100, set("Bench Press", 60.0, 8)),
                session(2, 200, set("Bench Press", 60.0, 10)),
                session(3, 300, set("Bench Press", 55.0, 12))
            )
        )

        assertEquals(mapOf(2L to 1), stats.recordsBySession)
        assertEquals(BestSet(60.0, 10, 200), stats.exercises.single().record)
    }

    @Test
    fun `sessions are sorted and exercise names merge case-insensitively`() {
        val stats = WorkoutStats.from(
            listOf(
                session(2, 200, set("bench press ", 70.0, 5)),
                session(1, 100, set("Bench Press", 60.0, 5))
            )
        )

        val exercise = stats.exercises.single()
        assertEquals("Bench Press", exercise.name)
        assertEquals(listOf(100L, 200L), exercise.sessionBests.map { it.achievedAt })
        assertEquals(mapOf(2L to 1), stats.recordsBySession)
    }

    @Test
    fun `incomplete sets are ignored and bodyweight is detected`() {
        val stats = WorkoutStats.from(
            listOf(session(1, 100, set("Push-up", 0.0, 20), set("Push-up", 20.0, 30, completed = false)))
        )

        val exercise = stats.exercises.single()
        assertTrue(exercise.isBodyweight)
        assertEquals(20, exercise.record.reps)
    }

    @Test
    fun `goal met requires every in-scope goal to be reached`() {
        val day = DayActivity(
            date = java.time.LocalDate.of(2026, 10, 1),
            repsByExercise = mapOf(1L to 50, 2L to 10),
            targetsByExercise = mapOf(1L to 50, 2L to 20, 3L to 0)
        )

        assertTrue(day.goalMet(1L))
        assertFalse(day.goalMet(2L))
        assertFalse(day.goalMet(null))
        assertFalse(day.goalMet(3L))
        assertEquals(60, day.reps(null))
    }
}
