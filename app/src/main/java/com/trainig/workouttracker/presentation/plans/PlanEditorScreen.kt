package com.trainig.workouttracker.presentation.plans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.PlanExercise
import com.trainig.workouttracker.core.domain.plan.ExerciseLibrary
import com.trainig.workouttracker.presentation.components.ExerciseIcon
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.presentation.components.NumberInputDialog
import com.trainig.workouttracker.presentation.components.NumberStepper
import com.trainig.workouttracker.presentation.components.formatRest
import com.trainig.workouttracker.presentation.components.formatWeight
import com.trainig.workouttracker.ui.theme.ExerciseAccents

private const val WEIGHT_STEP = 2.5
private const val REST_STEP = 15

/** Which numeric field of which exercise is being typed in. */
private data class NumberEdit(val index: Int, val field: Field) {
    enum class Field(val title: String) { SETS("Sets"), REPS("Reps"), WEIGHT("Weight (kg)"), REST("Rest (seconds)") }
}

@Composable
fun PlanEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlanEditorViewModel = hiltViewModel()
) {
    val plan by viewModel.plan.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var numberEdit by remember { mutableStateOf<NumberEdit?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                PlanEditorViewModel.UiEvent.Saved -> onNavigateBack()
                is PlanEditorViewModel.UiEvent.ShowError -> snackbarHostState.showSnackbar(event.error)
            }
        }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = if (viewModel.isNewPlan) "New Plan" else "Edit Plan",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::save,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Save")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add exercise") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = plan.name,
                    onValueChange = viewModel::setName,
                    label = { Text("Plan name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (plan.exercises.isNotEmpty()) {
                item {
                    Text(
                        text = "${plan.exercises.size} exercises · ~${plan.estimatedMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(plan.exercises) { index, exercise ->
                EditableExerciseCard(
                    index = index,
                    exercise = exercise,
                    isFirst = index == 0,
                    isLast = index == plan.exercises.lastIndex,
                    onChange = { transform -> viewModel.updateExercise(index, transform) },
                    onMoveUp = { viewModel.moveExercise(index, index - 1) },
                    onMoveDown = { viewModel.moveExercise(index, index + 1) },
                    onRemove = { viewModel.removeExercise(index) },
                    onEditNumber = { field -> numberEdit = NumberEdit(index, field) }
                )
            }

            if (plan.exercises.isEmpty()) {
                item {
                    Text(
                        text = "No exercises yet. Tap \"Add exercise\" to build your workout.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onAdd = { name, notes ->
                viewModel.addExercise(name, notes)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    numberEdit?.let { edit ->
        val exercise = plan.exercises.getOrNull(edit.index)
        if (exercise != null) {
            val isWeight = edit.field == NumberEdit.Field.WEIGHT
            val initial = when (edit.field) {
                NumberEdit.Field.SETS -> exercise.sets.toString()
                NumberEdit.Field.REPS -> exercise.reps.toString()
                NumberEdit.Field.WEIGHT -> exercise.weightKg.toString().removeSuffix(".0")
                NumberEdit.Field.REST -> exercise.restSeconds.toString()
            }
            NumberInputDialog(
                title = "${exercise.name}: ${edit.field.title}",
                initialValue = initial,
                decimal = isWeight,
                onConfirm = { value ->
                    viewModel.updateExercise(edit.index) {
                        when (edit.field) {
                            NumberEdit.Field.SETS -> it.copy(sets = value.toInt().coerceIn(1, 20))
                            NumberEdit.Field.REPS -> it.copy(reps = value.toInt().coerceIn(1, 200))
                            NumberEdit.Field.WEIGHT -> it.copy(weightKg = value)
                            NumberEdit.Field.REST -> it.copy(restSeconds = value.toInt())
                        }
                    }
                    numberEdit = null
                },
                onDismiss = { numberEdit = null }
            )
        }
    }
}

@Composable
private fun EditableExerciseCard(
    index: Int,
    exercise: PlanExercise,
    isFirst: Boolean,
    isLast: Boolean,
    onChange: ((PlanExercise) -> PlanExercise) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onEditNumber: (NumberEdit.Field) -> Unit
) {
    val accent = ExerciseAccents[index % ExerciseAccents.size]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExerciseIcon(name = exercise.name, color = accent, size = 36.dp)
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = exercise.name,
                    onValueChange = { name -> onChange { it.copy(name = name) } },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Column {
                    IconButton(onClick = onMoveUp, enabled = !isFirst, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = onMoveDown, enabled = !isLast, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove exercise", tint = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NumberStepper(
                    label = "Sets",
                    value = "${exercise.sets}",
                    color = accent,
                    onDecrease = { onChange { it.copy(sets = (it.sets - 1).coerceAtLeast(1)) } },
                    onIncrease = { onChange { it.copy(sets = it.sets + 1) } },
                    onValueClick = { onEditNumber(NumberEdit.Field.SETS) }
                )
                NumberStepper(
                    label = "Reps",
                    value = "${exercise.reps}",
                    color = accent,
                    onDecrease = { onChange { it.copy(reps = (it.reps - 1).coerceAtLeast(1)) } },
                    onIncrease = { onChange { it.copy(reps = it.reps + 1) } },
                    onValueClick = { onEditNumber(NumberEdit.Field.REPS) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NumberStepper(
                    label = "Weight",
                    value = formatWeight(exercise.weightKg),
                    color = accent,
                    onDecrease = { onChange { it.copy(weightKg = (it.weightKg - WEIGHT_STEP).coerceAtLeast(0.0)) } },
                    onIncrease = { onChange { it.copy(weightKg = it.weightKg + WEIGHT_STEP) } },
                    onValueClick = { onEditNumber(NumberEdit.Field.WEIGHT) }
                )
                NumberStepper(
                    label = "Rest",
                    value = formatRest(exercise.restSeconds),
                    color = accent,
                    onDecrease = { onChange { it.copy(restSeconds = (it.restSeconds - REST_STEP).coerceAtLeast(0)) } },
                    onIncrease = { onChange { it.copy(restSeconds = it.restSeconds + REST_STEP) } },
                    onValueClick = { onEditNumber(NumberEdit.Field.REST) }
                )
            }

            OutlinedTextField(
                value = exercise.notes,
                onValueChange = { notes -> onChange { it.copy(notes = notes) } },
                placeholder = { Text("Notes (optional)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AddExerciseDialog(
    onAdd: (name: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val matches = remember(query) { ExerciseLibrary.search(query).take(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search or type a name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(matches, key = { it.name }) { exercise ->
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAdd(exercise.name, exercise.cue) }
                                .padding(vertical = 10.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(query, "") },
                enabled = query.isNotBlank()
            ) {
                Text(
                    text = if (query.isBlank()) "Add custom" else "Add \"${query.trim().take(20)}\"",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
