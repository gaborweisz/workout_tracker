package com.trainig.workouttracker.presentation.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.TodayExerciseItem
import com.trainig.workouttracker.presentation.components.AccentProgressBar
import com.trainig.workouttracker.presentation.components.CelebrationDialog
import com.trainig.workouttracker.presentation.components.EmptyState
import com.trainig.workouttracker.presentation.components.ExerciseIcon
import com.trainig.workouttracker.presentation.components.GradientButton
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.presentation.components.ProgressRing
import com.trainig.workouttracker.ui.theme.BrandGradient
import com.trainig.workouttracker.ui.theme.Gold
import com.trainig.workouttracker.ui.theme.exerciseAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onNavigateToGoals: () -> Unit,
    onOpenDrawer: () -> Unit,
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
            GradientTopAppBar(
                title = "Today's Workout",
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
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
                    EmptyState(message = "No exercises with goals set (> 0).\nTap the edit icon to set today's goals!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "summary") {
                        TodaySummaryCard(items = visibleExercises)
                    }

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
            title = "Goal Achieved!",
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
fun TodaySummaryCard(items: List<TodayExerciseItem>) {
    val totalTarget = items.sumOf { it.targetReps }
    val totalDone = items.sumOf { it.completedReps.coerceAtMost(it.targetReps) }
    val goalsDone = items.count { it.isGoalAchieved }
    val progress = if (totalTarget > 0) totalDone.toFloat() / totalTarget else 0f
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")) }
    val motivation = when {
        progress >= 1f -> "All done, legend! 🔥"
        progress >= 0.5f -> "Over halfway there! 💪"
        progress > 0f -> "Great start, keep going!"
        else -> "Let's get moving! 🚀"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(BrandGradient))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = progress,
                modifier = Modifier.size(96.dp),
                colors = listOf(Color.White, Gold),
                trackColor = Color.White.copy(alpha = 0.25f)
            ) {
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = today,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = motivation,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$goalsDone of ${items.size} goals · $totalDone / $totalTarget reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
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
    val accent = exerciseAccent(item.exercise.id)
    val shape = RoundedCornerShape(20.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onSelect)
            .semantics { contentDescription = contentDesc }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, accent, shape)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                lerp(MaterialTheme.colorScheme.surface, accent, 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressRing(
                    progress = item.progressPercentage,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 5.dp,
                    colors = listOf(accent, lerp(accent, Color.White, 0.4f)),
                    trackColor = accent.copy(alpha = 0.15f)
                ) {
                    ExerciseIcon(name = item.exercise.name, color = accent)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.isGoalAchieved) {
                            Text(
                                text = " 🏆",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Text(
                        text = "${item.completedReps} / ${item.targetReps} reps",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (item.isGoalAchieved) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.isGoalAchieved) accent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onQuickDecrement,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = accent.copy(alpha = 0.15f),
                            contentColor = accent
                        )
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease by 1")
                    }
                    FilledIconButton(
                        onClick = onQuickIncrement,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = accent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase by 1")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            AccentProgressBar(progress = item.progressPercentage, color = accent)
        }
    }
}

@Composable
fun ControlPanel(
    hasSelection: Boolean,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit
) {
    val decreaseColors = listOf(Color(0xFF94A3B8), Color(0xFF64748B))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 8.dp
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ControlButton(
                            label = "1×",
                            icon = Icons.Default.KeyboardArrowDown,
                            onClick = { onDecrement(1) },
                            enabled = hasSelection,
                            colors = decreaseColors
                        )
                        ControlButton(
                            label = "5×",
                            icon = Icons.Default.KeyboardArrowDown,
                            onClick = { onDecrement(5) },
                            enabled = hasSelection,
                            colors = decreaseColors
                        )
                    }
                }

                VerticalDivider(modifier = Modifier.height(60.dp))

                // Increase buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Increase",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ControlButton(
                            label = "1×",
                            icon = Icons.Default.KeyboardArrowUp,
                            onClick = { onIncrement(1) },
                            enabled = hasSelection,
                            colors = BrandGradient
                        )
                        ControlButton(
                            label = "5×",
                            icon = Icons.Default.KeyboardArrowUp,
                            onClick = { onIncrement(5) },
                            enabled = hasSelection,
                            colors = BrandGradient
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
    enabled: Boolean,
    colors: List<Color>
) {
    GradientButton(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        modifier = Modifier
            .size(width = 64.dp, height = 56.dp)
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
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
