package com.trainig.workouttracker.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Goals : Screen("goals")
    data object Exercises : Screen("exercises")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

