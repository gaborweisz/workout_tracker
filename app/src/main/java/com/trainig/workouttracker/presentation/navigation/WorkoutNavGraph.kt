package com.trainig.workouttracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.trainig.workouttracker.presentation.exercises.ExercisesScreen
import com.trainig.workouttracker.presentation.goals.GoalsScreen
import com.trainig.workouttracker.presentation.history.HistoryScreen
import com.trainig.workouttracker.presentation.home.HomeScreen
import com.trainig.workouttracker.presentation.plans.PlanEditorScreen
import com.trainig.workouttracker.presentation.plans.PlanGeneratorScreen
import com.trainig.workouttracker.presentation.plans.PlansScreen
import com.trainig.workouttracker.presentation.settings.SettingsScreen
import com.trainig.workouttracker.presentation.stats.WorkoutStatsScreen
import com.trainig.workouttracker.presentation.workout.WorkoutScreen

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

        composable(Screen.Plans.route) {
            PlansScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuggestPlan = { navController.navigate(Screen.PlanGenerator.route) },
                onEditPlan = { planId -> navController.navigate(Screen.PlanEditor.createRoute(planId)) },
                onOpenWorkout = { sessionId -> navController.navigate(Screen.Workout.createRoute(sessionId)) },
                onOpenStats = { navController.navigate(Screen.WorkoutStats.route) }
            )
        }

        composable(Screen.WorkoutStats.route) {
            WorkoutStatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PlanGenerator.route) {
            PlanGeneratorScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlanSaved = { planId ->
                    // Replace the generator so "back" from the editor returns to the plan list
                    navController.navigate(Screen.PlanEditor.createRoute(planId)) {
                        popUpTo(Screen.PlanGenerator.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PlanEditor.route,
            arguments = listOf(navArgument(Screen.PlanEditor.ARG_PLAN_ID) { type = NavType.LongType })
        ) {
            PlanEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Workout.route,
            arguments = listOf(navArgument(Screen.Workout.ARG_SESSION_ID) { type = NavType.LongType })
        ) {
            WorkoutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

