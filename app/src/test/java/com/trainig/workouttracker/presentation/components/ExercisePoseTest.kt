package com.trainig.workouttracker.presentation.components

import com.trainig.workouttracker.core.domain.plan.ExerciseLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExercisePoseTest {

    @Test
    fun `specific phrases win over generic keywords`() {
        assertEquals(ExercisePose.LUNGE, ExercisePose.forName("Bulgarian Split Squat"))
        assertEquals(ExercisePose.CRUNCH, ExercisePose.forName("Hanging Leg Raise"))
        assertEquals(ExercisePose.SQUAT, ExercisePose.forName("Leg Press"))
        assertEquals(ExercisePose.DIP, ExercisePose.forName("Bench Dips"))
        assertEquals(ExercisePose.PUSH_UP, ExercisePose.forName("Pike Push-up"))
        assertEquals(ExercisePose.PULL_UP, ExercisePose.forName("Lat Pulldown"))
        assertEquals(ExercisePose.CALF_RAISE, ExercisePose.forName("Standing Calf Raise"))
    }

    @Test
    fun `user-typed daily exercises match`() {
        assertEquals(ExercisePose.PUSH_UP, ExercisePose.forName("pushups"))
        assertEquals(ExercisePose.SQUAT, ExercisePose.forName("Squats"))
        assertEquals(ExercisePose.CRUNCH, ExercisePose.forName("Sit ups"))
        assertNull(ExercisePose.forName("Yoga"))
    }

    @Test
    fun `every library exercise has a pictogram`() {
        val missing = ExerciseLibrary.all.filter { ExercisePose.forName(it.name) == null }.map { it.name }
        assertTrue("No pose for: $missing", missing.isEmpty())
    }
}
