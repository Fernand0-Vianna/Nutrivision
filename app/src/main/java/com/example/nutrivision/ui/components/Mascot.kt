package com.example.nutrivision.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.nutrivision.ui.theme.Ink
import com.example.nutrivision.ui.theme.Lime
import com.example.nutrivision.ui.theme.WaterFill
import kotlinx.coroutines.launch

/** Reações do mascote conforme o andamento do dia. */
enum class MascotMood {
    /** Meta do dia batida. */
    HAPPY,
    /** No ritmo, indo bem. */
    NEUTRAL,
    /** Abaixo do ritmo. */
    SAD,
    /** Nenhum registro ainda. */
    KO
}

/**
 * Desenha o mascote na área de 220x220, escalando para o tamanho do Canvas.
 * Compartilhado entre a abertura e a tela inicial.
 */
fun DrawScope.drawMascot(mood: MascotMood = MascotMood.HAPPY) {
    val s = size.minDimension / 220f
    val dx = (size.width - 220f * s) / 2f
    val dy = (size.height - 220f * s) / 2f

    withTransform({
        translate(dx, dy)
        scale(s, s, Offset.Zero)
    }) {
        // pernas
        drawRoundRect(Ink, Offset(80f, 150f), Size(24f, 40f), CornerRadius(10f, 10f))
        drawRoundRect(Ink, Offset(116f, 150f), Size(24f, 40f), CornerRadius(10f, 10f))
        // braços
        drawRoundRect(Ink, Offset(34f, 106f), Size(26f, 48f), CornerRadius(13f, 13f))
        drawRoundRect(Ink, Offset(160f, 106f), Size(26f, 48f), CornerRadius(13f, 13f))
        // cabeça
        drawRoundRect(Lime, Offset(52f, 44f), Size(116f, 108f), CornerRadius(26f, 26f))
        drawRoundRect(Ink, Offset(52f, 44f), Size(116f, 108f), CornerRadius(26f, 26f), style = Stroke(width = 10f))

        drawFace(mood)
    }
}

private fun DrawScope.drawFace(mood: MascotMood) {
    val eyeStroke = Stroke(width = 9f, cap = StrokeCap.Round)
    val line = Stroke(width = 8f, cap = StrokeCap.Round)

    when (mood) {
        MascotMood.HAPPY -> {
            // olhos sorrindo (∩ ∩)
            drawPath(Path().apply {
                moveTo(74f, 104f); quadraticBezierTo(87f, 86f, 100f, 104f)
                moveTo(120f, 104f); quadraticBezierTo(133f, 86f, 146f, 104f)
            }, Ink, style = eyeStroke)
            // sorriso
            drawPath(Path().apply {
                moveTo(90f, 126f); quadraticBezierTo(110f, 144f, 130f, 126f)
            }, Ink, style = line)
        }

        MascotMood.NEUTRAL -> {
            // olhos retos
            drawLine(Ink, Offset(76f, 102f), Offset(98f, 102f), strokeWidth = 9f, cap = StrokeCap.Round)
            drawLine(Ink, Offset(122f, 102f), Offset(144f, 102f), strokeWidth = 9f, cap = StrokeCap.Round)
            // boca reta
            drawLine(Ink, Offset(100f, 130f), Offset(120f, 130f), strokeWidth = 8f, cap = StrokeCap.Round)
        }

        MascotMood.SAD -> {
            // olhos caídos (∪ ∪)
            drawPath(Path().apply {
                moveTo(74f, 96f); quadraticBezierTo(87f, 114f, 100f, 96f)
                moveTo(120f, 96f); quadraticBezierTo(133f, 114f, 146f, 96f)
            }, Ink, style = eyeStroke)
            // boca triste
            drawPath(Path().apply {
                moveTo(90f, 136f); quadraticBezierTo(110f, 120f, 130f, 136f)
            }, Ink, style = line)
            // lágrima
            val tear = Path().apply {
                moveTo(152f, 108f)
                quadraticBezierTo(160f, 120f, 152f, 128f)
                quadraticBezierTo(144f, 120f, 152f, 108f)
            }
            drawPath(tear, WaterFill)
        }

        MascotMood.KO -> {
            // olhos em X
            fun cross(cx: Float, cy: Float) {
                val r = 14f
                drawLine(Ink, Offset(cx - r, cy - r), Offset(cx + r, cy + r), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(Ink, Offset(cx + r, cy - r), Offset(cx - r, cy + r), strokeWidth = 8f, cap = StrokeCap.Round)
            }
            cross(87f, 100f)
            cross(133f, 100f)
            // boquinha aberta
            drawCircle(Ink, radius = 7f, center = Offset(110f, 132f), style = Stroke(width = 7f))
        }
    }
}

/**
 * Mascote do app. [mood] escolhe a expressão; quando [animated], faz uma
 * flutuação contínua suave (sobe/desce) com uma leve respiração.
 */
@Composable
fun Mascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.HAPPY,
    animated: Boolean = true
) {
    if (!animated) {
        Canvas(modifier = modifier) { drawMascot(mood) }
        return
    }

    val bob = remember { Animatable(0f) }
    val breathe = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            bob.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        launch {
            breathe.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Canvas(
        modifier = modifier.graphicsLayer {
            translationY = -5.dp.toPx() * bob.value
            val s = 1f + 0.02f * breathe.value
            scaleX = s
            scaleY = s
        }
    ) {
        drawMascot(mood)
    }
}
