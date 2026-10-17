package com.trainig.workouttracker.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Goals : Screen("goals")
    data object Exercises : Screen("exercises")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Plans : Screen("plans")
    data object PlanGenerator : Screen("plan_generator")

    data object PlanEditor : Screen("plan_editor/{planId}") {
        const val ARG_PLAN_ID = "planId"

        /** Pass 0 to create a new plan. */
        fun createRoute(planId: Long) = "plan_editor/$planId"
    }

    data object Workout : Screen("workout/{sessionId}") {
        const val ARG_SESSION_ID = "sessionId"

        fun createRoute(sessionId: Long) = "workout/$sessionId"
    }
}
