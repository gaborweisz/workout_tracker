package com.trainig.workouttracker.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.trainig.workouttracker.data.local.entity.ExerciseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseBuilder {

    private var INSTANCE: WorkoutDatabase? = null

    fun getDatabase(context: Context): WorkoutDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                WorkoutDatabase::class.java,
                "workout_database"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with default exercises
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulateDatabase(database)
                            }
                        }
                    }
                })
                .build()
            INSTANCE = instance
            instance
        }
    }

    private suspend fun prepopulateDatabase(database: WorkoutDatabase) {
        val exerciseDao = database.exerciseDao()

        val defaultExercises = listOf(
            ExerciseEntity(name = "Pull-up"),
            ExerciseEntity(name = "Push-up"),
            ExerciseEntity(name = "Sit-up"),
            ExerciseEntity(name = "Squat"),
            ExerciseEntity(name = "Kettlebell Swing")
        )

        defaultExercises.forEach { exercise ->
            exerciseDao.insertExercise(exercise)
        }
    }
}

