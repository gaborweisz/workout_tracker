package com.trainig.workouttracker.presentation.workout

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.SessionSet
import com.trainig.workouttracker.core.domain.model.WorkoutSession
import com.trainig.workouttracker.presentation.components.CelebrationDialog
import com.trainig.workouttracker.presentation.components.ExerciseIcon
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.presentation.components.NumberInputDialog
import com.trainig.workouttracker.presentation.components.NumberStepper
import com.trainig.workouttracker.presentation.components.ProgressRing
import com.trainig.workouttracker.presentation.components.formatRest
import com.trainig.workouttracker.presentation.components.formatWeight
import com.trainig.workouttracker.ui.theme.BrandGradient
import com.trainig.workouttracker.ui.theme.ExerciseAccents
import com.trainig.workouttracker.ui.theme.Gold
import com.trainig.workouttracker.ui.theme.SuccessGradient
import kotlinx.coroutines.delay
import java.text.NumberFormat

private const val WEIGHT_STEP = 2.5

private data class SetEdit(val set: SessionSet, val isWeight: Boolean)

@Composable
fun WorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    val restTimer by viewModel.restTimer.collectAsState()
    val view = LocalView.current

    var showFinishDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var finishedSummary by remember { mutableStateOf<WorkoutViewModel.UiEvent.Finished?>(null) }
    var setEdit by remember { mutableStateOf<SetEdit?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                WorkoutViewModel.UiEvent.RestFinished -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                is WorkoutViewModel.UiEvent.Finished -> finishedSummary = event
                WorkoutViewModel.UiEvent.Discarded -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = session?.planName ?: "Workout",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDiscardDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Discard workout")
                    }
                    TextButton(
                        onClick = { showFinishDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Finish")
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = restTimer != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                restTimer?.let { timer ->
                    RestTimerBar(
                        timer = timer,
                        onAddTime = { viewModel.addRestTime(15) },
                        onSkip = viewModel::skipRest
                    )
                }
            }
        }
    ) { paddingValues ->
        val current = session
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "header") {
                    WorkoutHeader(session = current)
                }
                items(current.exercises, key = { it.first().exerciseIndex }) { sets ->
                    ExerciseSetsCard(
                        sets = sets,
                        onToggle = { set ->
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            viewModel.toggleSet(set)
                        },
                        onAdjustWeight = { set, delta -> viewModel.adjustWeight(set, delta) },
                        onAdjustReps = { set, delta -> viewModel.adjustReps(set, delta) },
                        onEditWeight = { set -> setEdit = SetEdit(set, isWeight = true) },
                        onEditReps = { set -> setEdit = SetEdit(set, isWeight = false) }
                    )
                }
            }
        }
    }

    if (showFinishDialog) {
        session?.let { current ->
            FinishDialog(
                session = current,
                onFinish = { updatePlan ->
                    showFinishDialog = false
                    viewModel.finish(updatePlan)
                },
                onDismiss = { showFinishDialog = false }
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard workout?") },
            text = { Text("Everything you logged in this session will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discard()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep going") }
            }
        )
    }

    setEdit?.let { edit ->
        NumberInputDialog(
            title = if (edit.isWeight) "Weight (kg)" else "Reps",
            initialValue = if (edit.isWeight) edit.set.weightKg.toString().removeSuffix(".0") else edit.set.reps.toString(),
            decimal = edit.isWeight,
            onConfirm = { value ->
                if (edit.isWeight) viewModel.setWeight(edit.set, value) else viewModel.setReps(edit.set, value.toInt())
                setEdit = null
            },
            onDismiss = { setEdit = null }
        )
    }

    finishedSummary?.let { summary ->
        val volume = NumberFormat.getIntegerInstance().format(summary.volumeKg.toLong())
        CelebrationDialog(
            title = "Workout Complete!",
            message = "${summary.completedSets} of ${summary.totalSets} sets done, $volume kg lifted in total. Great session!",
            onDismiss = {
                finishedSummary = null
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun WorkoutHeader(session: WorkoutSession) {
    // Re-read the clock every second for the elapsed-time display
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsedSeconds = ((now - session.startedAt) / 1000).toInt().coerceAtLeast(0)
    val progress = if (session.totalSets > 0) session.completedSets.toFloat() / session.totalSets else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(BrandGradient))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProgressRing(
            progress = progress,
            modifier = Modifier.size(84.dp),
            colors = listOf(Color.White, Gold),
            trackColor = Color.White.copy(alpha = 0.25f)
        ) {
            Text(
                text = "${session.completedSets}/${session.totalSets}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text("Elapsed", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Text(
                text = "%d:%02d:%02d".format(elapsedSeconds / 3600, elapsedSeconds / 60 % 60, elapsedSeconds % 60),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "${NumberFormat.getIntegerInstance().format(session.volumeKg.toLong())} kg lifted",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ExerciseSetsCard(
    sets: List<SessionSet>,
    onToggle: (SessionSet) -> Unit,
    onAdjustWeight: (SessionSet, Double) -> Unit,
    onAdjustReps: (SessionSet, Int) -> Unit,
    onEditWeight: (SessionSet) -> Unit,
    onEditReps: (SessionSet) -> Unit
) {
    val first = sets.first()
    val accent = ExerciseAccents[first.exerciseIndex % ExerciseAccents.size]
    val allDone = sets.all { it.completed }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExerciseIcon(name = first.exerciseName, color = accent)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(first.exerciseName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${sets.size} × ${first.targetReps} · rest ${formatRest(first.restSeconds)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (first.notes.isNotBlank()) {
                        Text(
                            text = first.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = accent
                        )
                    }
                }
                if (allDone) {
                    Text("✅", style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            sets.forEach { set ->
                SetRow(
                    set = set,
                    accent = accent,
                    onToggle = { onToggle(set) },
                    onAdjustWeight = { delta -> onAdjustWeight(set, delta) },
                    onAdjustReps = { delta -> onAdjustReps(set, delta) },
                    onEditWeight = { onEditWeight(set) },
                    onEditReps = { onEditReps(set) }
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    set: SessionSet,
    accent: Color,
    onToggle: () -> Unit,
    onAdjustWeight: (Double) -> Unit,
    onAdjustReps: (Int) -> Unit,
    onEditWeight: () -> Unit,
    onEditReps: () -> Unit
) {
    val background = if (set.completed) accent.copy(alpha = 0.10f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${set.setNumber}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.width(20.dp)
        )
        NumberStepper(
            label = "kg",
            value = formatWeight(set.weightKg).removeSuffix(" kg"),
            color = accent,
            onDecrease = { onAdjustWeight(-WEIGHT_STEP) },
            onIncrease = { onAdjustWeight(WEIGHT_STEP) },
            onValueClick = onEditWeight
        )
        NumberStepper(
            label = "reps",
            value = "${set.reps}",
            color = accent,
            onDecrease = { onAdjustReps(-1) },
            onIncrease = { onAdjustReps(1) },
            onValueClick = onEditReps
        )
        SetCheckButton(completed = set.completed, accent = accent, setNumber = set.setNumber, onClick = onToggle)
    }
}

@Composable
private fun SetCheckButton(completed: Boolean, accent: Color, setNumber: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .then(
                if (completed) {
                    Modifier.background(Brush.linearGradient(listOf(accent, lerp(accent, Color.White, 0.3f))))
                } else {
                    Modifier.border(2.dp, accent, CircleShape)
                }
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics {
                contentDescription = if (completed) "Set $setNumber done, tap to undo" else "Mark set $setNumber done"
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (completed) Color.White else accent.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun RestTimerBar(
    timer: WorkoutViewModel.RestTimer,
    onAddTime: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(shadowElevation = 12.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(SuccessGradient))
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(
                progress = timer.remainingSeconds.toFloat() / timer.totalSeconds.coerceAtLeast(1),
                modifier = Modifier.size(56.dp),
                strokeWidth = 5.dp,
                colors = listOf(Color.White, Color.White),
                trackColor = Color.White.copy(alpha = 0.25f)
            ) {
                Text(
                    text = formatRest(timer.remainingSeconds),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Rest", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    text = "Next: ${timer.nextUp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            TextButton(
                onClick = onAddTime,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) { Text("+15s") }
            TextButton(
                onClick = onSkip,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) { Text("Skip") }
        }
    }
}

@Composable
private fun FinishDialog(
    session: WorkoutSession,
    onFinish: (updatePlan: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var updatePlan by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish workout?") },
        text = {
            Column {
                Text("${session.completedSets} of ${session.totalSets} sets completed.")
                if (session.planId != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.clickable { updatePlan = !updatePlan },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = updatePlan, onCheckedChange = { updatePlan = it })
                        Text("Save today's weights & reps to the plan")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onFinish(updatePlan && session.planId != null) }) { Text("Finish") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep going") }
        }
    )
}
