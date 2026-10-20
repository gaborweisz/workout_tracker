package com.trainig.workouttracker.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.trainig.workouttracker.ui.theme.Gold
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/** One bar or point. [detail] is shown in the readout line when the entry is tapped. */
data class ChartEntry(
    val label: String,
    val value: Float,
    val color: Color,
    val detail: String,
    /** Bar charts draw a marker dot above marked bars (e.g. goal reached). */
    val marked: Boolean = false
)

/**
 * Vertical bar chart with a recessive grid. Tap or drag across the bars to read a value;
 * until then the readout shows [summary].
 */
@Composable
fun BarChart(
    entries: List<ChartEntry>,
    summary: String,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    markerColor: Color = Gold,
    axisLabel: (Float) -> String = { formatAxis(it) }
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(entries) { selected = null }
    val growth = rememberGrowth(entries)
    val measurer = rememberTextMeasurer()
    val axisStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val surface = MaterialTheme.colorScheme.surface

    Column(modifier = modifier) {
        ChartReadout(text = selected?.let { entries.getOrNull(it)?.detail } ?: summary)
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .semantics { contentDescription = summary }
                .selectByX(entries.size) { selected = it }
        ) {
            if (entries.isEmpty()) return@Canvas
            val ticks = niceTicks(0f, entries.maxOf { it.value }.coerceAtLeast(MIN_BAR_AXIS))
            val plot = drawAxes(ticks, entries.map { it.label }, measurer, axisStyle, gridColor, axisLabel)

            val slot = plot.width / entries.size
            val gap = 2.dp.toPx()
            val barWidth = (slot - gap).coerceAtMost(28.dp.toPx())
            val radius = 4.dp.toPx()
            entries.forEachIndexed { i, entry ->
                val barHeight = plot.height * (entry.value / ticks.last()) * growth
                if (barHeight <= 0f) return@forEachIndexed
                val left = plot.left + slot * i + (slot - barWidth) / 2
                val alpha = if (selected == null || selected == i) 1f else 0.45f
                // Round only the data end; the bar stays square on the baseline
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = left,
                            top = plot.bottom - barHeight,
                            right = left + barWidth,
                            bottom = plot.bottom,
                            topLeftCornerRadius = CornerRadius(radius.coerceAtMost(barHeight)),
                            topRightCornerRadius = CornerRadius(radius.coerceAtMost(barHeight))
                        )
                    )
                }
                drawPath(path, entry.color.copy(alpha = alpha))
                if (entry.marked) {
                    val dotRadius = 4.dp.toPx()
                    // Sit the dot just above the bar, but never above the top gridline
                    val center = Offset(left + barWidth / 2, (plot.bottom - barHeight - dotRadius - 3.dp.toPx()).coerceAtLeast(plot.top + dotRadius))
                    drawCircle(surface, radius = dotRadius + 1.5.dp.toPx(), center = center)
                    drawCircle(markerColor.copy(alpha = alpha), radius = dotRadius, center = center)
                }
            }
        }
    }
}

/** Line chart with point markers; the y-axis is fitted to the data range rather than starting at zero. */
@Composable
fun LineChart(
    entries: List<ChartEntry>,
    summary: String,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    axisLabel: (Float) -> String = { formatAxis(it) }
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(entries) { selected = null }
    val growth = rememberGrowth(entries)
    val measurer = rememberTextMeasurer()
    val axisStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val surface = MaterialTheme.colorScheme.surface

    Column(modifier = modifier) {
        ChartReadout(text = selected?.let { entries.getOrNull(it)?.detail } ?: summary)
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .semantics { contentDescription = summary }
                .selectByX(entries.size) { selected = it }
        ) {
            if (entries.isEmpty()) return@Canvas
            val values = entries.map { it.value }
            val ticks = niceTicks(values.min(), values.max())
            val plot = drawAxes(ticks, entries.map { it.label }, measurer, axisStyle, gridColor, axisLabel)

            val slot = plot.width / entries.size
            val points = entries.mapIndexed { i, entry ->
                val fraction = (entry.value - ticks.first()) / (ticks.last() - ticks.first())
                Offset(plot.left + slot * i + slot / 2, plot.bottom - plot.height * fraction * growth)
            }

            if (points.size > 1) {
                val line = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                val area = Path().apply {
                    addPath(line)
                    lineTo(points.last().x, plot.bottom)
                    lineTo(points.first().x, plot.bottom)
                    close()
                }
                drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), Color.Transparent), startY = plot.top, endY = plot.bottom))
                drawPath(line, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            selected?.let { points.getOrNull(it) }?.let { point ->
                drawLine(gridColor.copy(alpha = 0.6f), Offset(point.x, plot.top), Offset(point.x, plot.bottom), strokeWidth = 1.dp.toPx())
            }
            points.forEachIndexed { i, point ->
                val r = if (selected == i) 6.dp.toPx() else 4.dp.toPx()
                // Surface ring keeps markers legible where they sit on the line
                drawCircle(surface, radius = r + 2.dp.toPx(), center = point)
                drawCircle(entries[i].color, radius = r, center = point)
            }
        }
    }
}

/** Small swatch-and-label key, for charts where color carries meaning. */
@Composable
fun ChartLegend(items: List<Pair<Color, String>>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Horizontally scrolling single-choice chips, used as chart filters. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
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

/** Headline number with a caption and an emoji badge. */
@Composable
fun StatTile(
    label: String,
    value: String,
    emoji: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Keeps an all-zero or near-empty bar chart from getting fractional ticks. */
private const val MIN_BAR_AXIS = 4f

fun formatAxis(value: Float): String = when {
    value >= 10_000 -> "${(value / 1000).roundToInt()}k"
    value >= 1_000 -> "%.1fk".format(value / 1000).replace(".0k", "k")
    value == floor(value) -> value.toInt().toString()
    else -> "%.1f".format(value)
}

@Composable
private fun ChartReadout(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Fraction 0..1 the marks grow through each time the data changes. */
@Composable
private fun rememberGrowth(key: Any): Float {
    val growth = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { growth.animateTo(1f, tween(durationMillis = 700)) }
    return growth.value
}

/** Maps taps and horizontal drags to the column under the finger. */
private fun Modifier.selectByX(count: Int, onSelect: (Int) -> Unit): Modifier =
    if (count == 0) this else this
        .pointerInput(count) {
            detectTapGestures { offset -> onSelect(columnAt(offset.x, size.width.toFloat(), count)) }
        }
        .pointerInput(count) {
            detectHorizontalDragGestures { change, _ ->
                onSelect(columnAt(change.position.x, size.width.toFloat(), count))
            }
        }

// The y-axis gutter makes columns start a little right of 0, but at tap precision an even split is close enough
private fun columnAt(x: Float, width: Float, count: Int): Int =
    (x / width * count).toInt().coerceIn(0, count - 1)

private data class PlotArea(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
}

/** Draws gridlines with y labels and sparse x labels; returns the area left for the marks. */
private fun DrawScope.drawAxes(
    ticks: List<Float>,
    labels: List<String>,
    measurer: TextMeasurer,
    style: TextStyle,
    gridColor: Color,
    axisLabel: (Float) -> String
): PlotArea {
    val yLabels = ticks.map { measurer.measure(axisLabel(it), style) }
    val gutter = yLabels.maxOf { it.size.width } + 6.dp.toPx()
    val xLabelHeight = measurer.measure("0", style).size.height
    val plot = PlotArea(
        left = gutter,
        top = yLabels.first().size.height / 2f,
        right = size.width,
        bottom = size.height - xLabelHeight - 4.dp.toPx()
    )

    ticks.forEachIndexed { i, tick ->
        val y = plot.bottom - plot.height * (tick - ticks.first()) / (ticks.last() - ticks.first())
        drawLine(gridColor, Offset(plot.left, y), Offset(plot.right, y), strokeWidth = 1.dp.toPx())
        drawText(yLabels[i], topLeft = Offset(gutter - 6.dp.toPx() - yLabels[i].size.width, y - yLabels[i].size.height / 2f))
    }

    // Label about six columns, always including the latest one
    val every = ceil(labels.size / 6f).toInt().coerceAtLeast(1)
    val slot = plot.width / labels.size
    labels.forEachIndexed { i, label ->
        if ((labels.lastIndex - i) % every != 0) return@forEachIndexed
        val layout = measurer.measure(label, style)
        val x = (plot.left + slot * i + slot / 2 - layout.size.width / 2f)
            .coerceIn(plot.left, size.width - layout.size.width)
        drawText(layout, topLeft = Offset(x, plot.bottom + 4.dp.toPx()))
    }
    return plot
}

/** Three or four round-numbered ticks spanning [min]..[max]. */
private fun niceTicks(min: Float, max: Float): List<Float> {
    val span = (max - min).takeIf { it > 0f } ?: max.coerceAtLeast(1f)
    val rough = span / 3f
    val magnitude = 10f.pow(floor(log10(rough)))
    val step = listOf(1f, 2f, 2.5f, 5f, 10f).map { it * magnitude }.first { it >= rough }
    val start = if (min == max) floor((min - step) / step).coerceAtLeast(0f) * step else floor(min / step) * step
    val end = ceil(max / step).coerceAtLeast(start / step + 1) * step
    return generateSequence(start) { it + step }.takeWhile { it <= end + step / 2 }.toList()
}
