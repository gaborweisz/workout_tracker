package com.trainig.workouttracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Movement families we have a pictogram for; picked from the exercise name. */
enum class ExercisePose {
    PUSH_UP, SQUAT, LUNGE, HINGE, BENCH_PRESS, OVERHEAD_PRESS, ROW, PULL_UP,
    CURL, DIP, CRUNCH, CALF_RAISE, BRIDGE, LATERAL_RAISE, RUN, JUMP;

    companion object {
        // Checked in order, so more specific phrases ("split squat", "leg raise") come before generic ones
        private val keywords: List<Pair<ExercisePose, List<String>>> = listOf(
            PULL_UP to listOf("pull-up", "pullup", "pull up", "chin-up", "chinup", "chin up", "pulldown", "pull-down"),
            PUSH_UP to listOf("push-up", "pushup", "push up", "press-up", "plank", "mountain"),
            DIP to listOf("dip"),
            LUNGE to listOf("lunge", "split squat", "step-up", "step up"),
            CALF_RAISE to listOf("calf"),
            CRUNCH to listOf("leg raise", "crunch", "sit-up", "situp", "sit up", "twist", "dead bug", "rollout", "abs", "core", "v-up"),
            BRIDGE to listOf("bridge", "hip thrust", "glute"),
            HINGE to listOf("deadlift", "hinge", "good morning", "leg curl", "nordic", "kettlebell swing"),
            SQUAT to listOf("squat", "leg press", "leg extension", "wall sit"),
            BENCH_PRESS to listOf("bench", "chest press", "fly", "flye"),
            ROW to listOf("row"),
            LATERAL_RAISE to listOf("lateral", "rear delt", "face pull", "front raise", "raise"),
            CURL to listOf("curl", "pushdown", "kickback"),
            OVERHEAD_PRESS to listOf("press", "extension", "shoulder", "snatch", "jerk"),
            JUMP to listOf("jump", "jack", "burpee", "skip", "rope", "hop"),
            RUN to listOf("run", "jog", "sprint", "walk", "bike", "cycl", "cardio", "treadmill", "stairs")
        )

        fun forName(name: String): ExercisePose? {
            val lower = name.lowercase()
            return keywords.firstOrNull { (_, words) -> words.any { it in lower } }?.first
        }
    }
}

/**
 * Exercise badge: a stick-figure pictogram of the movement on the accent gradient.
 * Falls back to the letter avatar when the name doesn't match a known movement.
 */
@Composable
fun ExerciseIcon(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val pose = ExercisePose.forName(name)
    if (pose == null) {
        ExerciseAvatar(name = name, color = color, modifier = modifier, size = size)
        return
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(lerp(color, Color.White, 0.2f), lerp(color, Color.Black, 0.2f))))
    ) {
        ExercisePictogram(
            pose = pose,
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.14f)
        )
    }
}

/** Draws [pose] in a unit square scaled to the canvas; line weight follows the canvas size. */
@Composable
fun ExercisePictogram(
    pose: ExercisePose,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val figure = figureFor(pose)
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val stroke = s * 0.085f
        fun p(o: Offset) = Offset(o.x * s, o.y * s)

        figure.props.forEach { line ->
            drawPolyline(line.map(::p), color.copy(alpha = 0.55f), stroke * 0.7f)
        }
        figure.weights.forEach { drawCircle(color = color.copy(alpha = 0.85f), radius = s * 0.065f, center = p(it)) }
        figure.limbs.forEach { line -> drawPolyline(line.map(::p), color, stroke) }
        drawCircle(color = color, radius = s * 0.085f, center = p(figure.head))
    }
}

private fun DrawScope.drawPolyline(points: List<Offset>, color: Color, width: Float) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** Joint coordinates in a 0..1 box; y grows downwards. Side views face right. */
private class Figure(
    val head: Offset,
    val limbs: List<List<Offset>>,
    /** Floor, bars and benches, drawn fainter behind the body. */
    val props: List<List<Offset>> = emptyList(),
    /** Plates and dumbbell heads. */
    val weights: List<Offset> = emptyList()
)

private fun o(x: Float, y: Float) = Offset(x, y)

private val floor = listOf(o(0.04f, 0.92f), o(0.96f, 0.92f))

private fun figureFor(pose: ExercisePose): Figure = figures.getValue(pose)

private val figures: Map<ExercisePose, Figure> = mapOf(
    ExercisePose.PUSH_UP to Figure(
        head = o(0.84f, 0.46f),
        limbs = listOf(
            listOf(o(0.74f, 0.52f), o(0.44f, 0.62f), o(0.10f, 0.76f)),
            listOf(o(0.72f, 0.53f), o(0.74f, 0.82f))
        ),
        props = listOf(listOf(o(0.04f, 0.86f), o(0.96f, 0.86f)))
    ),
    ExercisePose.SQUAT to Figure(
        head = o(0.50f, 0.18f),
        limbs = listOf(
            listOf(o(0.48f, 0.30f), o(0.34f, 0.56f), o(0.62f, 0.60f), o(0.56f, 0.88f), o(0.70f, 0.88f)),
            listOf(o(0.47f, 0.34f), o(0.82f, 0.36f))
        )
    ),
    ExercisePose.LUNGE to Figure(
        head = o(0.48f, 0.12f),
        limbs = listOf(
            listOf(o(0.48f, 0.24f), o(0.48f, 0.52f)),
            listOf(o(0.48f, 0.52f), o(0.72f, 0.58f), o(0.72f, 0.88f)),
            listOf(o(0.48f, 0.52f), o(0.32f, 0.82f), o(0.12f, 0.86f)),
            listOf(o(0.48f, 0.28f), o(0.56f, 0.50f))
        ),
        weights = listOf(o(0.57f, 0.55f))
    ),
    ExercisePose.HINGE to Figure(
        head = o(0.76f, 0.28f),
        limbs = listOf(
            listOf(o(0.66f, 0.35f), o(0.32f, 0.42f), o(0.40f, 0.64f), o(0.36f, 0.88f)),
            listOf(o(0.64f, 0.37f), o(0.62f, 0.68f))
        ),
        props = listOf(floor),
        weights = listOf(o(0.62f, 0.74f))
    ),
    ExercisePose.BENCH_PRESS to Figure(
        head = o(0.82f, 0.54f),
        limbs = listOf(
            listOf(o(0.72f, 0.60f), o(0.40f, 0.60f), o(0.22f, 0.48f), o(0.14f, 0.88f)),
            listOf(o(0.66f, 0.59f), o(0.66f, 0.26f))
        ),
        props = listOf(
            listOf(o(0.30f, 0.68f), o(0.92f, 0.68f)),
            listOf(o(0.40f, 0.68f), o(0.40f, 0.90f)),
            listOf(o(0.82f, 0.68f), o(0.82f, 0.90f)),
            listOf(o(0.52f, 0.22f), o(0.80f, 0.22f))
        ),
        weights = listOf(o(0.50f, 0.22f), o(0.82f, 0.22f))
    ),
    ExercisePose.OVERHEAD_PRESS to Figure(
        head = o(0.50f, 0.28f),
        limbs = listOf(
            listOf(o(0.50f, 0.38f), o(0.50f, 0.62f)),
            listOf(o(0.50f, 0.62f), o(0.38f, 0.90f)),
            listOf(o(0.50f, 0.62f), o(0.62f, 0.90f)),
            listOf(o(0.50f, 0.40f), o(0.30f, 0.32f), o(0.28f, 0.12f)),
            listOf(o(0.50f, 0.40f), o(0.70f, 0.32f), o(0.72f, 0.12f))
        ),
        props = listOf(listOf(o(0.12f, 0.10f), o(0.88f, 0.10f))),
        weights = listOf(o(0.12f, 0.10f), o(0.88f, 0.10f))
    ),
    ExercisePose.ROW to Figure(
        head = o(0.76f, 0.28f),
        limbs = listOf(
            listOf(o(0.66f, 0.35f), o(0.32f, 0.44f), o(0.42f, 0.64f), o(0.38f, 0.88f)),
            listOf(o(0.64f, 0.37f), o(0.50f, 0.52f), o(0.60f, 0.58f))
        ),
        props = listOf(floor),
        weights = listOf(o(0.62f, 0.60f))
    ),
    ExercisePose.PULL_UP to Figure(
        head = o(0.50f, 0.24f),
        limbs = listOf(
            listOf(o(0.24f, 0.08f), o(0.28f, 0.32f), o(0.50f, 0.36f), o(0.72f, 0.32f), o(0.76f, 0.08f)),
            listOf(o(0.50f, 0.34f), o(0.50f, 0.62f)),
            listOf(o(0.50f, 0.62f), o(0.44f, 0.90f)),
            listOf(o(0.50f, 0.62f), o(0.56f, 0.90f))
        ),
        props = listOf(listOf(o(0.08f, 0.08f), o(0.92f, 0.08f)))
    ),
    ExercisePose.CURL to Figure(
        head = o(0.50f, 0.16f),
        limbs = listOf(
            listOf(o(0.50f, 0.27f), o(0.50f, 0.58f)),
            listOf(o(0.50f, 0.58f), o(0.40f, 0.90f)),
            listOf(o(0.50f, 0.58f), o(0.60f, 0.90f)),
            listOf(o(0.50f, 0.30f), o(0.34f, 0.50f), o(0.24f, 0.34f)),
            listOf(o(0.50f, 0.30f), o(0.66f, 0.50f), o(0.76f, 0.34f))
        ),
        weights = listOf(o(0.22f, 0.32f), o(0.78f, 0.32f))
    ),
    ExercisePose.DIP to Figure(
        head = o(0.50f, 0.24f),
        limbs = listOf(
            listOf(o(0.50f, 0.34f), o(0.50f, 0.62f)),
            listOf(o(0.50f, 0.36f), o(0.26f, 0.30f), o(0.16f, 0.48f)),
            listOf(o(0.50f, 0.36f), o(0.74f, 0.30f), o(0.84f, 0.48f)),
            listOf(o(0.50f, 0.62f), o(0.42f, 0.84f)),
            listOf(o(0.50f, 0.62f), o(0.58f, 0.84f))
        ),
        props = listOf(
            listOf(o(0.16f, 0.48f), o(0.16f, 0.94f)),
            listOf(o(0.84f, 0.48f), o(0.84f, 0.94f))
        )
    ),
    ExercisePose.CRUNCH to Figure(
        head = o(0.76f, 0.46f),
        limbs = listOf(
            listOf(o(0.68f, 0.55f), o(0.46f, 0.78f), o(0.30f, 0.50f), o(0.14f, 0.78f)),
            listOf(o(0.66f, 0.57f), o(0.46f, 0.50f))
        ),
        props = listOf(listOf(o(0.04f, 0.84f), o(0.96f, 0.84f)))
    ),
    ExercisePose.CALF_RAISE to Figure(
        head = o(0.42f, 0.14f),
        limbs = listOf(
            listOf(o(0.42f, 0.25f), o(0.42f, 0.56f), o(0.42f, 0.78f), o(0.52f, 0.86f)),
            listOf(o(0.42f, 0.28f), o(0.48f, 0.54f))
        ),
        props = listOf(
            listOf(o(0.30f, 0.88f), o(0.70f, 0.88f)),
            listOf(o(0.80f, 0.60f), o(0.80f, 0.22f)),
            listOf(o(0.70f, 0.32f), o(0.80f, 0.22f), o(0.90f, 0.32f))
        )
    ),
    ExercisePose.BRIDGE to Figure(
        head = o(0.86f, 0.74f),
        limbs = listOf(
            listOf(o(0.76f, 0.78f), o(0.46f, 0.54f), o(0.26f, 0.48f), o(0.22f, 0.82f)),
            listOf(o(0.74f, 0.78f), o(0.56f, 0.82f))
        ),
        props = listOf(listOf(o(0.04f, 0.86f), o(0.96f, 0.86f)))
    ),
    ExercisePose.LATERAL_RAISE to Figure(
        head = o(0.50f, 0.18f),
        limbs = listOf(
            listOf(o(0.50f, 0.29f), o(0.50f, 0.60f)),
            listOf(o(0.50f, 0.60f), o(0.40f, 0.90f)),
            listOf(o(0.50f, 0.60f), o(0.60f, 0.90f)),
            listOf(o(0.12f, 0.34f), o(0.50f, 0.33f), o(0.88f, 0.34f))
        ),
        weights = listOf(o(0.10f, 0.34f), o(0.90f, 0.34f))
    ),
    ExercisePose.RUN to Figure(
        head = o(0.60f, 0.14f),
        limbs = listOf(
            listOf(o(0.58f, 0.25f), o(0.46f, 0.52f)),
            listOf(o(0.46f, 0.52f), o(0.66f, 0.64f), o(0.60f, 0.88f)),
            listOf(o(0.46f, 0.52f), o(0.36f, 0.72f), o(0.14f, 0.70f)),
            listOf(o(0.56f, 0.30f), o(0.70f, 0.42f), o(0.80f, 0.30f)),
            listOf(o(0.56f, 0.30f), o(0.40f, 0.40f), o(0.32f, 0.52f))
        )
    ),
    ExercisePose.JUMP to Figure(
        head = o(0.50f, 0.18f),
        limbs = listOf(
            listOf(o(0.50f, 0.29f), o(0.50f, 0.56f)),
            listOf(o(0.20f, 0.10f), o(0.50f, 0.33f), o(0.80f, 0.10f)),
            listOf(o(0.50f, 0.56f), o(0.24f, 0.88f)),
            listOf(o(0.50f, 0.56f), o(0.76f, 0.88f))
        )
    )
)
