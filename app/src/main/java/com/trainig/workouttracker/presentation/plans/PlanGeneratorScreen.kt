package com.trainig.workouttracker.presentation.plans

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.Equipment
import com.trainig.workouttracker.core.domain.model.ExperienceLevel
import com.trainig.workouttracker.core.domain.model.PlanExercise
import com.trainig.workouttracker.core.domain.model.TrainingGoal
import com.trainig.workouttracker.core.domain.model.WorkoutFocus
import com.trainig.workouttracker.presentation.components.ExerciseAvatar
import com.trainig.workouttracker.presentation.components.GradientButton
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.presentation.components.formatRest
import com.trainig.workouttracker.presentation.components.formatWeight
import com.trainig.workouttracker.ui.theme.ExerciseAccents
import com.trainig.workouttracker.ui.theme.Teal
import com.trainig.workouttracker.ui.theme.Violet

private val DURATIONS = listOf(30, 45, 60, 75, 90)

@Composable
fun PlanGeneratorScreen(
    onNavigateBack: () -> Unit,
    onPlanSaved: (Long) -> Unit,
    viewModel: PlanGeneratorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val preferences = uiState.preferences

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PlanGeneratorViewModel.UiEvent.PlanSaved -> onPlanSaved(event.planId)
                is PlanGeneratorViewModel.UiEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = event.error,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
            }
        }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Suggest a Workout",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ChoiceRow(
                    title = "Goal",
                    options = TrainingGoal.entries,
                    selected = preferences.goal,
                    label = { it.label },
                    onSelect = { goal -> viewModel.updatePreferences { it.copy(goal = goal) } }
                )
            }
            item {
                ChoiceRow(
                    title = "Focus",
                    options = WorkoutFocus.entries,
                    selected = preferences.focus,
                    label = { it.label },
                    onSelect = { focus -> viewModel.updatePreferences { it.copy(focus = focus) } }
                )
            }
            item {
                ChoiceRow(
                    title = "Equipment",
                    options = Equipment.entries,
                    selected = preferences.equipment,
                    label = { it.label },
                    onSelect = { equipment -> viewModel.updatePreferences { it.copy(equipment = equipment) } }
                )
            }
            item {
                ChoiceRow(
                    title = "Experience",
                    options = ExperienceLevel.entries,
                    selected = preferences.experience,
                    label = { it.label },
                    onSelect = { level -> viewModel.updatePreferences { it.copy(experience = level) } }
                )
            }
            item {
                ChoiceRow(
                    title = "Time",
                    options = DURATIONS,
                    selected = preferences.durationMinutes,
                    label = { "$it min" },
                    onSelect = { minutes -> viewModel.updatePreferences { it.copy(durationMinutes = minutes) } }
                )
            }
            item {
                OutlinedTextField(
                    value = preferences.notes,
                    onValueChange = { notes -> viewModel.updatePreferences { it.copy(notes = notes) } },
                    label = { Text("Anything else? (AI only)") },
                    placeholder = { Text("e.g. sore left knee, want more arm work") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GradientButton(
                        onClick = viewModel::suggest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("✨ Suggest a plan", style = MaterialTheme.typography.titleMedium)
                    }
                    GradientButton(
                        onClick = viewModel::askAi,
                        enabled = apiKey.isNotBlank() && !uiState.isLoading,
                        colors = listOf(Violet, Teal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Claude is thinking…", style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            Text("🤖 Ask Claude AI", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    if (apiKey.isBlank()) {
                        Text(
                            text = "Add your Anthropic API key in Settings to enable AI suggestions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.suggestion?.let { plan ->
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plan.name, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "${plan.exercises.size} exercises · ~${plan.estimatedMinutes} min" +
                                    if (uiState.source == PlanGeneratorViewModel.Source.AI) " · by Claude" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = viewModel::regenerate, enabled = !uiState.isLoading) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
                        }
                    }
                }
                itemsIndexed(plan.exercises) { index, exercise ->
                    SuggestedExerciseRow(index = index, exercise = exercise)
                }
                item {
                    GradientButton(
                        onClick = viewModel::acceptSuggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Use this plan & customize", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceRow(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun SuggestedExerciseRow(index: Int, exercise: PlanExercise) {
    val accent = ExerciseAccents[index % ExerciseAccents.size]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ExerciseAvatar(name = exercise.name, color = accent)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = buildString {
                        append("${exercise.sets} × ${exercise.reps}")
                        if (exercise.weightKg > 0) append(" @ ${formatWeight(exercise.weightKg)}")
                        append(" · rest ${formatRest(exercise.restSeconds)}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )
                if (exercise.notes.isNotBlank()) {
                    Text(
                        text = exercise.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
