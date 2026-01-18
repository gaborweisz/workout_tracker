package com.trainig.workouttracker.presentation.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.TodayExerciseItem
import com.trainig.workouttracker.presentation.components.CelebrationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGoals: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val todayExercises by viewModel.todayExercises.collectAsState()
    val visibleExercises = remember(todayExercises) {
        todayExercises.filter { it.hasGoal && it.targetReps > 0 }
    }
    val selectedExerciseId by viewModel.selectedExerciseId.collectAsState()
    val allGoalsCompleted by viewModel.allGoalsCompleted.collectAsState()
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showCelebration by remember { mutableStateOf<String?>(null) }
    var showAllGoalsComplete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collect { event ->
            when (event) {
                is HomeViewModel.CelebrationEvent.GoalAchieved -> {
                    showCelebration = event.exerciseName
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
                HomeViewModel.CelebrationEvent.AllGoalsCompleted -> {
                    showAllGoalsComplete = true
                }
            }
        }
    }

    LaunchedEffect(allGoalsCompleted, visibleExercises) {
        if (allGoalsCompleted && visibleExercises.isNotEmpty()) {
            showAllGoalsComplete = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Workout") },
                actions = {
                    IconButton(onClick = onNavigateToGoals) {
                        Icon(Icons.Default.Edit, contentDescription = "Set Goals")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (visibleExercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No exercises with goals set (> 0).\nTap the edit icon to set today's goals!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleExercises, key = { it.exercise.id }) { item ->
                        ExerciseCard(
                            item = item,
                            isSelected = item.exercise.id == selectedExerciseId,
                            onSelect = { viewModel.selectExercise(item.exercise.id) },
                            onQuickIncrement = {
                                viewModel.selectExercise(item.exercise.id)
                                viewModel.incrementProgress(1)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            },
                            onQuickDecrement = {
                                viewModel.selectExercise(item.exercise.id)
                                viewModel.incrementProgress(-1)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        )
                    }
                }

                ControlPanel(
                    hasSelection = selectedExerciseId != null,
                    onIncrement = { delta ->
                        viewModel.incrementProgress(delta)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    },
                    onDecrement = { delta ->
                        viewModel.incrementProgress(-delta)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                )
            }
        }
    }

    showCelebration?.let { exerciseName ->
        CelebrationDialog(
            title = "🏆 Goal Achieved!",
            message = "Congratulations! You've completed your $exerciseName goal!",
            onDismiss = { showCelebration = null }
        )
    }

    if (showAllGoalsComplete) {
        CelebrationDialog(
            title = "🎉 All Goals Complete!",
            message = "Amazing work! You've achieved all your goals for today!",
            onDismiss = { showAllGoalsComplete = false }
        )
    }
}

@Composable
fun ExerciseCard(
    item: TodayExerciseItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onQuickIncrement: () -> Unit,
    onQuickDecrement: () -> Unit
) {
    val contentDesc = "${item.exercise.name}, ${item.completedReps} of ${item.targetReps} completed"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .semantics { contentDescription = contentDesc }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (item.isGoalAchieved) {
                    Text(
                        text = "🏆",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.hasGoal) {
                        "${item.completedReps} / ${item.targetReps}"
                    } else {
                        "${item.completedReps} (No goal set)"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.isGoalAchieved) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onQuickDecrement,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease by 1")
                    }
                    IconButton(
                        onClick = onQuickIncrement,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase by 1")
                    }
                }
            }

            if (item.hasGoal) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                )
            }
        }
    }
}

@Composable
fun ControlPanel(
    hasSelection: Boolean,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasSelection) {
                Text(
                    text = "Select an exercise first",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Decrease",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ControlButton(
                            label = "1×",
                            icon = Icons.Default.KeyboardArrowDown,
                            onClick = { onDecrement(1) },
                            enabled = hasSelection
                        )
                        ControlButton(
                            label = "5×",
                            icon = Icons.Default.KeyboardArrowDown,
                            onClick = { onDecrement(5) },
                            enabled = hasSelection
                        )
                    }
                }

                Divider(
                    modifier = Modifier
                        .height(60.dp)
                        .width(1.dp)
                )

                // Increase buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Increase",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ControlButton(
                            label = "1×",
                            icon = Icons.Default.KeyboardArrowUp,
                            onClick = { onIncrement(1) },
                            enabled = hasSelection
                        )
                        ControlButton(
                            label = "5×",
                            icon = Icons.Default.KeyboardArrowUp,
                            onClick = { onIncrement(5) },
                            enabled = hasSelection
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(width = 60.dp, height = 50.dp)
            .semantics {
                contentDescription = "$label button"
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
