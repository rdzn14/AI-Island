package com.aiisland.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Mint = Color(0xFF7CF0C8)
private val Amber = Color(0xFFFFB454)
private val Gray = Color(0xFF9AA3B2)
private val HeadColor = Color(0xFFE6EBF0)
private val ScreenColor = Color(0xFF0B0F14)

@Composable
fun Robot(mood: Mood, boxSize: Dp, modifier: Modifier = Modifier, tilt: Float = 0f) {
    val bobMs = when (mood) {
        Mood.SLEEP -> 4200
        Mood.SLEEPY -> 3200
        else -> 1800
    }
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(bobMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bobValue"
    )
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseValue"
    )
    val spin by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing)),
        label = "spinValue"
    )
    val wob by rememberInfiniteTransition(label = "wob").animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse),
        label = "wobValue"
    )
    val tiltAnim by animateFloatAsState(tilt, tween(180), label = "tilt")
    val blink = remember { Animatable(1f) }
    val gaze = remember { Animatable(0f) }

    LaunchedEffect(mood) {
        while (true) {
            delay(Random.nextLong(2500L, 5000L))
            if (mood == Mood.NEUTRAL || mood == Mood.HAPPY) {
                blink.animateTo(0.1f, tween(70))
                blink.animateTo(1f, tween(110))
            }
            if (Random.nextInt(3) == 0) {
                gaze.animateTo(if (Random.nextBoolean()) -1f else 1f, tween(250))
                delay(700)
                gaze.animateTo(0f, tween(300))
            }
        }
    }

    Canvas(modifier.size(boxSize)) {
        val s = this.size.minDimension / 64f

        fun rr(c: Color, x: Float, y: Float, w: Float, h: Float, r: Float) {
            drawRoundRect(c, Offset(x * s, y * s), Size(w * s, h * s), CornerRadius(r * s))
        }

        val angle = if (mood == Mood.DIZZY) wob else 0f
        rotate(angle, Offset(32f * s, 40f * s)) {
            translate(top = -2.5f * s * bob) {
                val antenna = when (mood) {
                    Mood.SLEEPY -> Amber
                    Mood.DIZZY -> Amber
                    Mood.SLEEP -> Gray
                    else -> Mint
                }
                drawLine(
                    Gray,
                    Offset(32f * s, 14f * s),
                    Offset(32f * s, 7f * s),
                    strokeWidth = 2.5f * s,
                    cap = StrokeCap.Round
                )
                drawCircle(antenna.copy(alpha = pulse), 3.5f * s, Offset(32f * s, 5f * s))
                rr(Gray, 2f, 26f, 6f, 14f, 3f)
                rr(Gray, 56f, 26f, 6f, 14f, 3f)
                rr(HeadColor, 7f, 14f, 50f, 42f, 16f)
                rr(ScreenColor, 13f, 21f, 38f, 28f, 11f)

                val still = mood == Mood.SLEEP || mood == Mood.DIZZY
                val gx = if (still) 0f else (gaze.value + tiltAnim).coerceIn(-1f, 1f) * 3.5f

                if (mood == Mood.HAPPY) {
                    for (x in floatArrayOf(20f, 35f)) {
                        val p = Path().apply {
                            moveTo((x + gx) * s, 39f * s)
                            quadraticBezierTo((x + 4.5f + gx) * s, 31f * s, (x + 9f + gx) * s, 39f * s)
                        }
                        drawPath(p, Mint, style = Stroke(width = 3.5f * s, cap = StrokeCap.Round))
                    }
                } else if (mood == Mood.SLEEP) {
                    for (x in floatArrayOf(21f, 36f)) {
                        val p = Path().apply {
                            moveTo(x * s, 36f * s)
                            quadraticBezierTo((x + 3.5f) * s, 41f * s, (x + 7f) * s, 36f * s)
                        }
                        drawPath(p, Mint, style = Stroke(width = 3f * s, cap = StrokeCap.Round))
                    }
                } else if (mood == Mood.DIZZY) {
                    for (x in floatArrayOf(21f, 36f)) {
                        rotate(spin, Offset((x + 3.5f) * s, 35f * s)) {
                            rr(Mint, x, 29f, 7f, 12f, 3.5f)
                        }
                    }
                } else {
                    val open = if (mood == Mood.SLEEPY) 0.4f else blink.value
                    val eyeH = 12f * open
                    for (x in floatArrayOf(21f, 36f)) {
                        rr(Mint, x + gx, 35f - eyeH / 2f, 7f, eyeH, minOf(3.5f, eyeH / 2f))
                    }
                }
            }
        }
    }
}
