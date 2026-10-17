package com.trainig.workouttracker.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trainig.workouttracker.ui.theme.BrandGradient
import com.trainig.workouttracker.ui.theme.ExerciseAccents
import com.trainig.workouttracker.ui.theme.Gold
import kotlin.random.Random

@Composable
fun CelebrationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "celebration")
    val trophyScale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "trophyScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Brush.linearGradient(BrandGradient)),
                contentAlignment = Alignment.Center
            ) {
                Confetti(modifier = Modifier.matchParentSize())
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(trophyScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color.White, Gold.copy(alpha = 0.6f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏆", style = MaterialTheme.typography.displaySmall)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                GradientButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Awesome!", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private class ConfettiPiece(
    val x: Float,
    val offset: Float,
    val speed: Float,
    val size: Float,
    val spin: Float,
    val color: Color
)

/** Endlessly falling confetti that wraps from bottom to top. */
@Composable
private fun Confetti(modifier: Modifier = Modifier, count: Int = 40) {
    val pieces = remember {
        val colors = ExerciseAccents + Color.White + Gold
        List(count) {
            ConfettiPiece(
                x = Random.nextFloat(),
                offset = Random.nextFloat(),
                speed = 0.6f + Random.nextFloat() * 0.8f,
                size = 4f + Random.nextFloat() * 6f,
                spin = Random.nextFloat() * 720f - 360f,
                color = colors.random()
            )
        }
    }
    val time by rememberInfiniteTransition(label = "confetti").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "confettiTime"
    )

    Canvas(modifier = modifier) {
        pieces.forEach { piece ->
            val progress = (piece.offset + time * piece.speed) % 1f
            val pieceSize = piece.size.dp.toPx()
            val position = Offset(size.width * piece.x, size.height * progress - pieceSize)
            rotate(degrees = piece.spin * progress, pivot = position) {
                drawRect(
                    color = piece.color,
                    topLeft = position,
                    size = Size(pieceSize, pieceSize * 0.5f)
                )
            }
        }
    }
}
