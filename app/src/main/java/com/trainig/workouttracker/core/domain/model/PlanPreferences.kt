package com.trainig.workouttracker.core.domain.model

enum class TrainingGoal(val label: String) {
    STRENGTH("Strength"),
    MUSCLE("Build muscle"),
    ENDURANCE("Endurance"),
    GENERAL("General fitness")
}

enum class MuscleGroup {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, QUADS, HAMSTRINGS, GLUTES, CALVES, CORE
}

/** Each focus lists muscle "slots" in priority order; the generator fills them one exercise at a time. */
enum class WorkoutFocus(val label: String, val slots: List<MuscleGroup>) {
    FULL_BODY(
        "Full body",
        listOf(
            MuscleGroup.QUADS, MuscleGroup.CHEST, MuscleGroup.BACK, MuscleGroup.HAMSTRINGS,
            MuscleGroup.SHOULDERS, MuscleGroup.CORE, MuscleGroup.BICEPS, MuscleGroup.TRICEPS,
            MuscleGroup.GLUTES, MuscleGroup.CALVES
        )
    ),
    UPPER(
        "Upper body",
        listOf(
            MuscleGroup.CHEST, MuscleGroup.BACK, MuscleGroup.SHOULDERS, MuscleGroup.BACK,
            MuscleGroup.CHEST, MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS
        )
    ),
    LOWER(
        "Lower body",
        listOf(
            MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.QUADS,
            MuscleGroup.CALVES, MuscleGroup.CORE, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE
        )
    ),
    PUSH(
        "Push",
        listOf(
            MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.CHEST, MuscleGroup.TRICEPS,
            MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.CHEST
        )
    ),
    PULL(
        "Pull",
        listOf(
            MuscleGroup.BACK, MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.SHOULDERS,
            MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.CORE
        )
    ),
    LEGS(
        "Legs",
        listOf(
            MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.QUADS, MuscleGroup.GLUTES,
            MuscleGroup.CALVES, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE
        )
    )
}

/** Ordered from most to least equipment: a level can use anything at or below it. */
enum class Equipment(val label: String) {
    FULL_GYM("Full gym"),
    DUMBBELLS("Dumbbells only"),
    BODYWEIGHT("Bodyweight")
}

enum class ExperienceLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

data class PlanPreferences(
    val goal: TrainingGoal = TrainingGoal.MUSCLE,
    val focus: WorkoutFocus = WorkoutFocus.FULL_BODY,
    val equipment: Equipment = Equipment.FULL_GYM,
    val experience: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val durationMinutes: Int = 60,
    /** Free-text wishes; only the AI generator can act on these. */
    val notes: String = ""
)
