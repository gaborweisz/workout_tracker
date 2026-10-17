package com.trainig.workouttracker.presentation.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.model.WorkoutSession
import com.trainig.workouttracker.presentation.components.EmptyState
import com.trainig.workouttracker.presentation.components.ExerciseAvatar
import com.trainig.workouttracker.presentation.components.GradientButton
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.presentation.components.ProgressRing
import com.trainig.workouttracker.ui.theme.BrandGradient
import com.trainig.workouttracker.ui.theme.Gold
import com.trainig.workouttracker.ui.theme.SuccessGradient
import com.trainig.workouttracker.ui.theme.exerciseAccent
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PlansScreen(
    onNavigateBack: () -> Unit,
    onSuggestPlan: () -> Unit,
    onEditPlan: (Long) -> Unit,
    onOpenWorkout: (Long) -> Unit,
    viewModel: PlansViewModel = hiltViewModel()
) {
    val plans by viewModel.plans.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var planToStart by remember { mutableStateOf<WorkoutPlan?>(null) }
    var planToDelete by remember { mutableStateOf<WorkoutPlan?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PlansViewModel.UiEvent.WorkoutStarted -> onOpenWorkout(event.sessionId)
                is PlansViewModel.UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Gym Plans",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEditPlan(0L) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New plan") }
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
            // One item, so the banner loading in later can't push the list's scroll anchor out of view
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    activeSession?.let { session ->
                        ActiveWorkoutBanner(session = session, onResume = { onOpenWorkout(session.id) })
                    }
                    SuggestPlanCard(onClick = onSuggestPlan)
                }
            }

            if (plans.isEmpty()) {
                item(key = "empty") {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EmptyState(message = "No plans yet.\nGet a suggestion or build your own!")
                    }
                }
            } else {
                item(key = "plansHeader") { SectionHeader("My plans") }
                items(plans, key = { it.id }) { plan ->
                    PlanCard(
                        plan = plan,
                        onClick = { onEditPlan(plan.id) },
                        onStart = { if (activeSession != null) planToStart = plan else viewModel.startWorkout(plan) },
                        onDelete = { planToDelete = plan }
                    )
                }
            }

            if (recentSessions.isNotEmpty()) {
                item(key = "recentHeader") { SectionHeader("Recent workouts") }
                items(recentSessions, key = { "session-${it.id}" }) { session ->
                    RecentWorkoutRow(session = session)
                }
            }
        }
    }

    planToStart?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToStart = null },
            title = { Text("Workout in progress") },
            text = { Text("You haven't finished '${activeSession?.planName}'. Discard it and start '${plan.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startWorkout(plan)
                    planToStart = null
                }) { Text("Discard & start") }
            },
            dismissButton = {
                TextButton(onClick = { planToStart = null }) { Text("Cancel") }
            }
        )
    }

    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = { Text("Delete plan?") },
            text = { Text("Delete '${plan.name}'? Your past workouts with it are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlan(plan.id)
                    planToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ActiveWorkoutBanner(session: WorkoutSession, onResume: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(SuccessGradient))
            .clickable(onClick = onResume)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProgressRing(
            progress = if (session.totalSets > 0) session.completedSets.toFloat() / session.totalSets else 0f,
            modifier = Modifier.size(56.dp),
            strokeWidth = 6.dp,
            colors = listOf(Color.White, Gold),
            trackColor = Color.White.copy(alpha = 0.25f)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Workout in progress", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
            Text(session.planName, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(
                "${session.completedSets} of ${session.totalSets} sets done",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        Text("Resume", style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun SuggestPlanCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(BrandGradient))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "✨", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Suggest a workout", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(
                "Tell us your goal and time; get a plan you can tweak",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: WorkoutPlan,
    onClick: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = exerciseAccent(plan.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExerciseAvatar(name = plan.name, color = accent, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${plan.exercises.size} exercises · ~${plan.estimatedMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete plan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plan.exercises.joinToString(" · ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit")
                }
                GradientButton(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentWorkoutRow(session: WorkoutSession) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }
    val date = Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault()).format(formatter)
    val minutes = session.finishedAt?.let { ((it - session.startedAt) / 60_000).toInt() }
    val volume = NumberFormat.getIntegerInstance().format(session.volumeKg.toLong())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = if (session.completedSets == session.totalSets) "🏆" else "💪", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.planName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    listOfNotNull(date, minutes?.let { "$it min" }, "${session.completedSets}/${session.totalSets} sets", "$volume kg").joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
