package com.trainig.workouttracker.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.trainig.workouttracker.ui.theme.BrandGradient

/** Top app bar drawn over the brand gradient with white content. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(BrandGradient))
    ) {
        TopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

/** Circular progress ring with a sweeping gradient and animated fill. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    colors: List<Color> = BrandGradient,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "ringProgress"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke)
            )
            // Rotate so the sweep gradient and the arc both start at 12 o'clock
            rotate(-90f) {
                drawArc(
                    brush = Brush.sweepGradient(colors + colors.first()),
                    startAngle = 0f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

/** Rounded progress bar in a single accent color. */
@Composable
fun AccentProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "barProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color, lerp(color, Color.White, 0.3f))))
        )
    }
}

/** Colored circle with the exercise's initial. */
@Composable
fun ExerciseAvatar(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(lerp(color, Color.White, 0.2f), lerp(color, Color.Black, 0.2f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().take(1).uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** Button with a gradient background; dims when disabled. */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = BrandGradient,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(Brush.linearGradient(colors))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            content()
        }
    }
}

/** Hand-drawn dumbbell with a soft glow and sparkles, used for headers and empty states. */
@Composable
fun DumbbellIllustration(
    modifier: Modifier = Modifier,
    colors: List<Color> = BrandGradient,
    barColor: Color = Color(0xFF9CA3AF)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.first().copy(alpha = 0.28f), Color.Transparent),
                center = center,
                radius = w / 2
            ),
            radius = w / 2
        )

        // Bar and grip
        drawRoundRect(
            color = barColor,
            topLeft = Offset(w * 0.10f, h * 0.47f),
            size = Size(w * 0.80f, h * 0.06f),
            cornerRadius = CornerRadius(h * 0.03f)
        )
        drawRoundRect(
            color = lerp(barColor, Color.Black, 0.3f),
            topLeft = Offset(w * 0.38f, h * 0.455f),
            size = Size(w * 0.24f, h * 0.09f),
            cornerRadius = CornerRadius(h * 0.045f)
        )

        // Plates: (left edge, width, height) as fractions, mirrored on the right side
        val plateBrush = Brush.verticalGradient(colors, startY = h * 0.2f, endY = h * 0.8f)
        listOf(
            Triple(0.15f, 0.07f, 0.34f),
            Triple(0.22f, 0.09f, 0.52f)
        ).forEach { (x, pw, ph) ->
            listOf(x, 1f - x - pw).forEach { left ->
                drawRoundRect(
                    brush = plateBrush,
                    topLeft = Offset(w * left, h * (0.5f - ph / 2)),
                    size = Size(w * pw, h * ph),
                    cornerRadius = CornerRadius(w * 0.025f)
                )
            }
        }

        // Sparkles
        listOf(
            Triple(0.18f, 0.18f, colors[1 % colors.size]),
            Triple(0.84f, 0.24f, colors.last()),
            Triple(0.78f, 0.82f, colors.first()),
            Triple(0.24f, 0.80f, colors.last())
        ).forEachIndexed { i, (sx, sy, color) ->
            drawCircle(color = color, radius = w * (0.018f + 0.008f * (i % 2)), center = Offset(w * sx, h * sy))
        }
    }
}

/** Illustration plus a friendly message, for screens with nothing to show yet. */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DumbbellIllustration(modifier = Modifier.size(160.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
