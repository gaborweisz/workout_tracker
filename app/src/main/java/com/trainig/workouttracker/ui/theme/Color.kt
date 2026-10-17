package com.trainig.workouttracker.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette
val Violet = Color(0xFF6C4DFF)
val Magenta = Color(0xFFE6398F)
val Orange = Color(0xFFFF8A3D)
val Teal = Color(0xFF14B8A6)
val Green = Color(0xFF22C55E)
val Gold = Color(0xFFFFC53D)

// Gradients used for headers, buttons and progress rings
val BrandGradient = listOf(Violet, Magenta, Orange)
val SuccessGradient = listOf(Teal, Green)

// Each exercise gets its own accent color, picked by id so it stays stable across screens
val ExerciseAccents = listOf(
    Color(0xFF6C4DFF), // violet
    Color(0xFFE6398F), // magenta
    Color(0xFFFF8A3D), // orange
    Color(0xFF14B8A6), // teal
    Color(0xFF3B82F6), // blue
    Color(0xFF22C55E), // green
    Color(0xFFF59E0B), // amber
    Color(0xFFEF4444)  // red
)

fun exerciseAccent(exerciseId: Long): Color =
    ExerciseAccents[Math.floorMod(exerciseId, ExerciseAccents.size.toLong()).toInt()]
