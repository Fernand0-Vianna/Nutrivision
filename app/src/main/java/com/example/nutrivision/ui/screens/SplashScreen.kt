package com.example.nutrivision.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.ui.components.drawMascot
import com.example.nutrivision.ui.theme.ArchivoFamily
import com.example.nutrivision.ui.theme.Bone
import com.example.nutrivision.ui.theme.Elevated
import com.example.nutrivision.ui.theme.Ink
import com.example.nutrivision.ui.theme.Lime
import com.example.nutrivision.ui.theme.Muted
import com.example.nutrivision.ui.theme.Primary
import com.example.nutrivision.ui.theme.Secondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Abertura do app. Mascote com entrada suave (escala + fade), flutuação
 * contínua e uma barra de progresso que representa o carregamento.
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val scale = remember { Animatable(0.78f) }
    val fade = remember { Animatable(0f) }
    val floatY = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { fade.animateTo(1f, tween(durationMillis = 520, delayMillis = 80)) }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            floatY.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1700, delayMillis = 250, easing = FastOutSlowInEasing)
            )
        }
        delay(2300)
        onDone()
    }

    Box(modifier = Modifier.fillMaxSize().background(Bone)) {
        // Selo de versão no topo
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .graphicsLayer { this.alpha = fade.value }
                .background(Lime, RoundedCornerShape(999.dp))
                .border(2.dp, Ink, RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.Bolt, contentDescription = null, tint = Ink, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NUTRIVISION 1.2",
                fontFamily = ArchivoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Ink
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { this.alpha = fade.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .size(184.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationY = -10.dp.toPx() * floatY.value
                    }
            ) {
                drawMascot()
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "NUTRIVISION",
                fontFamily = ArchivoFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                letterSpacing = (-0.5).sp,
                color = Ink
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "SEU DIÁRIO ALIMENTAR",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                color = Secondary
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp)
                .graphicsLayer { this.alpha = fade.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NbProgressBarLocal(progress = progress.value)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TOQUE PARA CONTINUAR",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = Muted
            )
        }
    }
}

@Composable
private fun NbProgressBarLocal(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(Elevated, RoundedCornerShape(4.dp))
            .border(2.dp, Ink, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(Primary)
        )
    }
}

