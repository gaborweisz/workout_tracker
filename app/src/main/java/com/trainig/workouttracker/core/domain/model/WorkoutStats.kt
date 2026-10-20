package com.trainig.workouttracker.core.domain.model

/** Heaviest completed set of an exercise; reps break ties between equal weights. */
data class BestSet(
    val weightKg: Double,
    val reps: Int,
    val achievedAt: Long
) : Comparable<BestSet> {
    override fun compareTo(other: BestSet): Int =
        compareValuesBy(this, other, { it.weightKg }, { it.reps })
}

data class ExerciseHistory(
    val name: String,
    /** Best set of each workout the exercise was done in, oldest first. */
    val sessionBests: List<BestSet>
) {
    val record: BestSet get() = sessionBests.max()

    /** Nothing was ever logged with weight, so progress is tracked by reps instead. */
    val isBodyweight: Boolean get() = sessionBests.all { it.weightKg == 0.0 }
}

data class WorkoutStats(
    /** Finished sessions, oldest first. */
    val sessions: List<WorkoutSession>,
    /** Exercises done at least once, most frequently done first. */
    val exercises: List<ExerciseHistory>,
    /** Session id -> number of exercises that beat their previous best in it. */
    val recordsBySession: Map<Long, Int>
) {
    val totalVolumeKg: Double get() = sessions.sumOf { it.volumeKg }
    val totalRecords: Int get() = recordsBySession.values.sum()

    companion object {
        fun from(finishedSessions: List<WorkoutSession>): WorkoutStats {
            val sessions = finishedSessions.sortedBy { it.startedAt }
            // Keyed case-insensitively so "Bench press" and "Bench Press" count as one exercise
            val bests = linkedMapOf<String, Pair<String, MutableList<BestSet>>>()
            val records = mutableMapOf<Long, Int>()

            sessions.forEach { session ->
                session.sets
                    .filter { it.completed }
                    .groupBy { it.exerciseName.trim().lowercase() }
                    .forEach { (key, sets) ->
                        val best = sets.map { BestSet(it.weightKg, it.reps, session.startedAt) }.max()
                        val history = bests.getOrPut(key) { sets.first().exerciseName.trim() to mutableListOf() }.second
                        // Only beating an earlier best is a record; the first time doesn't count
                        if (history.isNotEmpty() && best > history.max()) {
                            records[session.id] = (records[session.id] ?: 0) + 1
                        }
                        history += best
                    }
            }

            return WorkoutStats(
                sessions = sessions,
                exercises = bests.values
                    .map { (name, history) -> ExerciseHistory(name, history) }
                    .sortedByDescending { it.sessionBests.size },
                recordsBySession = records
            )
        }
    }
}
