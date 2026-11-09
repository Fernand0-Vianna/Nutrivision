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
import kotlinx.coroutines.launch

/**
 * Desenha o mascote na área de 220x220, escalando para o tamanho do Canvas.
 * Compartilhado entre a abertura e a tela inicial.
 */
fun DrawScope.drawMascot() {
    val s = size.minDimension / 220f
    val dx = (size.width - 220f * s) / 2f
    val dy = (size.height - 220f * s) / 2f

    withTransform({
        translate(dx, dy)
        scale(s, s, Offset.Zero)
    }) {
        // pernas
        drawRoundRect(
            color = Ink,
            topLeft = Offset(80f, 150f),
            size = Size(24f, 40f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = Ink,
            topLeft = Offset(116f, 150f),
            size = Size(24f, 40f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // braços
        drawRoundRect(
            color = Ink,
            topLeft = Offset(34f, 106f),
            size = Size(26f, 48f),
            cornerRadius = CornerRadius(13f, 13f)
        )
        drawRoundRect(
            color = Ink,
            topLeft = Offset(160f, 106f),
            size = Size(26f, 48f),
            cornerRadius = CornerRadius(13f, 13f)
        )
        // cabeça
        drawRoundRect(
            color = Lime,
            topLeft = Offset(52f, 44f),
            size = Size(116f, 108f),
            cornerRadius = CornerRadius(26f, 26f)
        )
        drawRoundRect(
            color = Ink,
            topLeft = Offset(52f, 44f),
            size = Size(116f, 108f),
            cornerRadius = CornerRadius(26f, 26f),
            style = Stroke(width = 10f)
        )
        // olhos sorridentes
        val eyes = Path().apply {
            moveTo(74f, 104f); quadraticBezierTo(87f, 86f, 100f, 104f)
            moveTo(120f, 104f); quadraticBezierTo(133f, 86f, 146f, 104f)
        }
        drawPath(path = eyes, color = Ink, style = Stroke(width = 9f, cap = StrokeCap.Round))
    }
}

/**
 * Mascote do app. Quando [animated], faz uma flutuação contínua suave
 * (sobe/desce) com uma leve respiração.
 */
@Composable
fun Mascot(
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    if (!animated) {
        Canvas(modifier = modifier) { drawMascot() }
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
        drawMascot()
    }
}
