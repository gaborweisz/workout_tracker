package com.trainig.workouttracker.presentation.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.presentation.components.EmptyState
import com.trainig.workouttracker.presentation.components.ExerciseAvatar
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.ui.theme.exerciseAccent

@Composable
fun ExercisesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToArchive by remember { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExercisesViewModel.UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ExercisesViewModel.UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.error)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Manage Exercises",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                text = { Text("Add Exercise") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    onEdit = { editingExercise = exercise },
                    onArchive = { exerciseToArchive = exercise }
                )
            }

            if (exercises.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(message = "No exercises yet.\nTap + to add one!")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ExerciseInputDialog(
            title = "Add Exercise",
            initialName = "",
            onConfirm = { name ->
                viewModel.addExercise(name)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingExercise?.let { exercise ->
        ExerciseInputDialog(
            title = "Edit Exercise",
            initialName = exercise.name,
            onConfirm = { name ->
                viewModel.updateExercise(exercise.copy(name = name))
                editingExercise = null
            },
            onDismiss = { editingExercise = null }
        )
    }

    exerciseToArchive?.let { exercise ->
        AlertDialog(
            onDismissRequest = { exerciseToArchive = null },
            title = { Text("Archive Exercise?") },
            text = { Text("Are you sure you want to archive '${exercise.name}'? It will be hidden but history will be preserved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.archiveExercise(exercise.id)
                        exerciseToArchive = null
                    }
                ) {
                    Text("Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToArchive = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    val accent = exerciseAccent(exercise.id)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseAvatar(name = exercise.name, color = accent, size = 44.dp)

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = accent)
                }
                IconButton(onClick = onArchive) {
                    Icon(Icons.Default.Delete, contentDescription = "Archive", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun ExerciseInputDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
