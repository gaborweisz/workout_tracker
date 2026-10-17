package com.trainig.workouttracker.core.domain.plan

import com.trainig.workouttracker.core.domain.model.Equipment
import com.trainig.workouttracker.core.domain.model.MuscleGroup
import com.trainig.workouttracker.core.domain.model.MuscleGroup.*

data class LibraryExercise(
    val name: String,
    val muscle: MuscleGroup,
    /** Least equipment level that still allows this exercise. */
    val requires: Equipment,
    val compound: Boolean,
    val cue: String = ""
) {
    fun isAvailableWith(equipment: Equipment): Boolean = requires.ordinal >= equipment.ordinal
}

object ExerciseLibrary {

    private val gym = Equipment.FULL_GYM
    private val db = Equipment.DUMBBELLS
    private val bw = Equipment.BODYWEIGHT

    val all: List<LibraryExercise> = listOf(
        // Chest
        LibraryExercise("Barbell Bench Press", CHEST, gym, true, "Shoulder blades pinned, bar to mid-chest"),
        LibraryExercise("Machine Chest Press", CHEST, gym, true),
        LibraryExercise("Cable Fly", CHEST, gym, false, "Slight bend in the elbows, squeeze at the middle"),
        LibraryExercise("Dumbbell Bench Press", CHEST, db, true),
        LibraryExercise("Incline Dumbbell Press", CHEST, db, true, "Bench at 30°"),
        LibraryExercise("Dumbbell Fly", CHEST, db, false),
        LibraryExercise("Push-up", CHEST, bw, true, "Body in one straight line"),
        LibraryExercise("Dips", CHEST, bw, true, "Lean forward slightly for chest"),
        // Back
        LibraryExercise("Deadlift", BACK, gym, true, "Brace hard, bar close to the shins"),
        LibraryExercise("Barbell Row", BACK, gym, true, "Hinge to ~45°, pull to the belly button"),
        LibraryExercise("Lat Pulldown", BACK, gym, true, "Pull elbows down to your ribs"),
        LibraryExercise("Seated Cable Row", BACK, gym, true),
        LibraryExercise("One-Arm Dumbbell Row", BACK, db, true),
        LibraryExercise("Pull-up", BACK, bw, true, "Full hang at the bottom"),
        LibraryExercise("Inverted Row", BACK, bw, true),
        // Shoulders
        LibraryExercise("Overhead Press", SHOULDERS, gym, true, "Squeeze glutes, press straight up"),
        LibraryExercise("Face Pull", SHOULDERS, gym, false, "Pull rope towards your forehead"),
        LibraryExercise("Seated Dumbbell Press", SHOULDERS, db, true),
        LibraryExercise("Lateral Raise", SHOULDERS, db, false, "Lead with the elbows, no swinging"),
        LibraryExercise("Rear Delt Fly", SHOULDERS, db, false),
        LibraryExercise("Pike Push-up", SHOULDERS, bw, true),
        // Biceps
        LibraryExercise("Barbell Curl", BICEPS, gym, false),
        LibraryExercise("Cable Curl", BICEPS, gym, false),
        LibraryExercise("Dumbbell Curl", BICEPS, db, false),
        LibraryExercise("Hammer Curl", BICEPS, db, false),
        LibraryExercise("Chin-up", BICEPS, bw, true),
        // Triceps
        LibraryExercise("Close-Grip Bench Press", TRICEPS, gym, true),
        LibraryExercise("Triceps Pushdown", TRICEPS, gym, false, "Elbows locked at your sides"),
        LibraryExercise("Overhead Dumbbell Extension", TRICEPS, db, false),
        LibraryExercise("Bench Dips", TRICEPS, bw, false),
        LibraryExercise("Diamond Push-up", TRICEPS, bw, false),
        // Quads
        LibraryExercise("Back Squat", QUADS, gym, true, "Brace, knees track over toes, hit depth"),
        LibraryExercise("Leg Press", QUADS, gym, true),
        LibraryExercise("Leg Extension", QUADS, gym, false),
        LibraryExercise("Goblet Squat", QUADS, db, true),
        LibraryExercise("Bulgarian Split Squat", QUADS, db, true, "Rear foot on a bench"),
        LibraryExercise("Bodyweight Squat", QUADS, bw, true),
        LibraryExercise("Walking Lunge", QUADS, bw, true),
        // Hamstrings
        LibraryExercise("Romanian Deadlift", HAMSTRINGS, gym, true, "Push hips back, soft knees"),
        LibraryExercise("Lying Leg Curl", HAMSTRINGS, gym, false),
        LibraryExercise("Dumbbell Romanian Deadlift", HAMSTRINGS, db, true),
        LibraryExercise("Nordic Curl", HAMSTRINGS, bw, false, "Lower as slowly as you can"),
        LibraryExercise("Single-Leg Hip Hinge", HAMSTRINGS, bw, false),
        // Glutes
        LibraryExercise("Barbell Hip Thrust", GLUTES, gym, true, "Chin tucked, squeeze at the top"),
        LibraryExercise("Dumbbell Step-up", GLUTES, db, true),
        LibraryExercise("Glute Bridge", GLUTES, bw, false),
        // Calves
        LibraryExercise("Standing Calf Raise", CALVES, gym, false),
        LibraryExercise("Dumbbell Calf Raise", CALVES, db, false),
        LibraryExercise("Bodyweight Calf Raise", CALVES, bw, false, "Pause at the top"),
        // Core
        LibraryExercise("Cable Crunch", CORE, gym, false),
        LibraryExercise("Russian Twist", CORE, db, false),
        LibraryExercise("Hanging Leg Raise", CORE, bw, false, "No swinging"),
        LibraryExercise("Dead Bug", CORE, bw, false),
        LibraryExercise("Ab Wheel Rollout", CORE, bw, false)
    )

    fun search(query: String): List<LibraryExercise> =
        if (query.isBlank()) all else all.filter { it.name.contains(query.trim(), ignoreCase = true) }
}
