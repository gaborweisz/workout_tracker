package com.trainig.workouttracker.presentation.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.BestSet
import com.trainig.workouttracker.core.domain.model.ExerciseHistory
import com.trainig.workouttracker.core.domain.model.WorkoutStats
import com.trainig.workouttracker.presentation.components.BarChart
import com.trainig.workouttracker.presentation.components.ChartEntry
import com.trainig.workouttracker.presentation.components.ChartLegend
import com.trainig.workouttracker.presentation.components.EmptyState
import com.trainig.workouttracker.presentation.components.ExerciseIcon
import com.trainig.workouttracker.presentation.components.FilterChipRow
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.presentation.components.LineChart
import com.trainig.workouttracker.presentation.components.StatTile
import com.trainig.workouttracker.presentation.components.formatAxis
import com.trainig.workouttracker.ui.theme.Gold
import com.trainig.workouttracker.ui.theme.Magenta
import com.trainig.workouttracker.ui.theme.Violet
import com.trainig.workouttracker.ui.theme.exerciseAccent
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** How many recent workouts the volume chart shows. */
private const val VOLUME_SESSIONS = 12

@Composable
fun WorkoutStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutStatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Workout Stats",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val current = stats ?: return@Scaffold
        if (current.sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(message = "No finished workouts yet.\nComplete one to see your stats!")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "tiles") { SummaryTiles(current) }
            item(key = "volume") { VolumeCard(current) }

            current.exercises.firstOrNull { it.name == selectedExercise }?.let { exercise ->
                item(key = "progress") {
                    ExerciseProgressCard(
                        exercises = current.exercises,
                        selected = exercise,
                        onSelect = viewModel::selectExercise
                    )
                }
            }

            item(key = "recordsHeader") {
                Text(
                    text = "Personal records",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(current.exercises.sortedBy { it.name.lowercase() }, key = { it.name }) { exercise ->
                RecordRow(exercise)
            }
        }
    }
}

@Composable
private fun SummaryTiles(stats: WorkoutStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile(
            label = "Workouts",
            value = "${stats.sessions.size}",
            emoji = "🏋️",
            accent = Violet,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            label = "kg lifted",
            value = formatAxis(stats.totalVolumeKg.toFloat()),
            emoji = "💪",
            accent = Magenta,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            label = "PRs set",
            value = "${stats.totalRecords}",
            emoji = "🏅",
            accent = Gold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VolumeCard(stats: WorkoutStats) {
    val barColor = MaterialTheme.colorScheme.primary
    val labelFormat = remember { DateTimeFormatter.ofPattern("MMM d") }
    val sessions = stats.sessions.takeLast(VOLUME_SESSIONS)
    val entries = remember(stats, barColor) {
        sessions.map { session ->
            val records = stats.recordsBySession[session.id] ?: 0
            val date = session.startedAt.toLocalDate()
            ChartEntry(
                label = date.format(labelFormat),
                value = session.volumeKg.toFloat(),
                color = barColor,
                detail = buildString {
                    append("${session.planName} · ${date.format(labelFormat)} · ${formatKg(session.volumeKg)}")
                    if (records > 0) append(" · 🏅 $records PR")
                },
                marked = records > 0
            )
        }
    }
    val average = sessions.map { it.volumeKg }.average()

    ChartCard(title = "Volume per workout") {
        BarChart(
            entries = entries,
            summary = "Last ${sessions.size} workouts · avg ${formatKg(average)}",
            axisLabel = { formatAxis(it) }
        )
        if (entries.any { it.marked }) {
            ChartLegend(items = listOf(barColor to "Volume (kg)", Gold to "New PR"))
        }
    }
}

@Composable
private fun ExerciseProgressCard(
    exercises: List<ExerciseHistory>,
    selected: ExerciseHistory,
    onSelect: (String) -> Unit
) {
    val color = exerciseAccent(selected.name.lowercase().hashCode().toLong())
    val labelFormat = remember { DateTimeFormatter.ofPattern("MMM d") }
    val entries = remember(selected, color) {
        selected.sessionBests.map { best ->
            ChartEntry(
                label = best.achievedAt.toLocalDate().format(labelFormat),
                value = if (selected.isBodyweight) best.reps.toFloat() else best.weightKg.toFloat(),
                color = color,
                detail = "${best.achievedAt.toLocalDate().format(labelFormat)} · ${formatSet(best)}"
            )
        }
    }
    val first = selected.sessionBests.first()
    val record = selected.record
    val unit = if (selected.isBodyweight) "reps" else "kg"
    val change = if (selected.isBodyweight) (record.reps - first.reps).toDouble() else record.weightKg - first.weightKg

    ChartCard(title = if (selected.isBodyweight) "Best reps per workout" else "Top weight per workout") {
        if (exercises.size > 1) {
            FilterChipRow(
                options = exercises.map { it.name },
                selected = selected.name,
                label = { it },
                onSelect = onSelect
            )
        }
        LineChart(
            entries = entries,
            summary = if (entries.size < 2) {
                "${selected.name} · ${formatSet(record)} · do it again to see a trend"
            } else {
                "${selected.name} · ${if (change > 0) "+" else ""}${formatAxis(change.toFloat())} $unit since ${first.achievedAt.toLocalDate().format(labelFormat)}"
            },
            color = color
        )
    }
}

@Composable
private fun RecordRow(exercise: ExerciseHistory) {
    val accent = exerciseAccent(exercise.name.lowercase().hashCode().toLong())
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ExerciseIcon(name = exercise.name, color = accent)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "${exercise.sessionBests.size} workouts · set ${exercise.record.achievedAt.toLocalDate().format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("🏅", style = MaterialTheme.typography.titleMedium)
                Text(formatSet(exercise.record), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

private fun Long.toLocalDate() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun formatKg(kg: Double) = "${NumberFormat.getIntegerInstance().format(kg.toLong())} kg"

private fun formatSet(best: BestSet) =
    if (best.weightKg == 0.0) "${best.reps} reps" else "${formatAxis(best.weightKg.toFloat())} kg × ${best.reps}"
