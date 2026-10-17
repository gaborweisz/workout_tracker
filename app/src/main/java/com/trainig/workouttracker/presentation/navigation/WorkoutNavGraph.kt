package com.trainig.workouttracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.trainig.workouttracker.presentation.exercises.ExercisesScreen
import com.trainig.workouttracker.presentation.goals.GoalsScreen
import com.trainig.workouttracker.presentation.history.HistoryScreen
import com.trainig.workouttracker.presentation.home.HomeScreen
import com.trainig.workouttracker.presentation.settings.SettingsScreen

@Composable
fun WorkoutNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(Screen.Goals.route) {
            GoalsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Exercises.route) {
            ExercisesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

