package com.trainig.workouttracker.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trainig.workouttracker.core.domain.model.DailySummary
import com.trainig.workouttracker.presentation.components.AccentProgressBar
import com.trainig.workouttracker.presentation.components.EmptyState
import com.trainig.workouttracker.presentation.components.ExerciseAvatar
import com.trainig.workouttracker.presentation.components.GradientTopAppBar
import com.trainig.workouttracker.ui.theme.BrandGradient
import com.trainig.workouttracker.ui.theme.ExerciseAccents
import com.trainig.workouttracker.ui.theme.exerciseAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyDates by viewModel.historyDates.collectAsState()
    val selectedDateSummary by viewModel.selectedDateSummary.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "History",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (historyDates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(message = "No history yet.\nStart tracking your workouts!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyDates, key = { it.toString() }) { date ->
                    HistoryDateCard(
                        date = date,
                        onClick = { viewModel.loadSummaryForDate(date) }
                    )
                }
            }
        }
    }

    selectedDateSummary?.let { summary ->
        HistoryDetailDialog(
            summary = summary,
            onDismiss = { viewModel.clearSelectedDate() }
        )
    }
}

@Composable
fun HistoryDateCard(
    date: LocalDate,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    // Tint each calendar tile by day of week for a bit of variety
    val accent = ExerciseAccents[date.dayOfWeek.value % ExerciseAccents.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarTile(date = date, color = accent)

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View details",
                tint = accent
            )
        }
    }
}

@Composable
private fun CalendarTile(date: LocalDate, color: Color) {
    Column(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMM")).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = lerp(color, Color.Black, 0.2f)
            )
        }
    }
}

@Composable
fun HistoryDetailDialog(
    summary: DailySummary,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(summary.date.format(formatter))
                Text(
                    text = "${summary.goalsAchieved} of ${summary.totalGoals} goals achieved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(BrandGradient))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Total Reps",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "${summary.totalReps}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Exercises:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(summary.exerciseDetails) { detail ->
                    val accent = exerciseAccent(detail.exerciseId)
                    val progress = if (detail.targetReps > 0) {
                        detail.completedReps.toFloat() / detail.targetReps
                    } else {
                        0f
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExerciseAvatar(name = detail.exerciseName, color = accent, size = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = detail.exerciseName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${detail.completedReps} / ${detail.targetReps}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (detail.isGoalAchieved) {
                                    Text(
                                        text = "🏆",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                            if (detail.targetReps > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AccentProgressBar(progress = progress, color = accent)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
