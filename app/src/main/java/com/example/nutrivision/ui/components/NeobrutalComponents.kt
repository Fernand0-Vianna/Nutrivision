package com.example.nutrivision.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import com.example.nutrivision.ui.theme.*

/**
 * Sombra "dura" no estilo Tuckii: um retângulo preto deslocado para a direita
 * e para baixo, sem desfoque. O deslocamento horizontal é maior para dar a
 * sensação de uma borda saliente mais grossa no lado direito.
 */
fun Modifier.nbHardShadow(
    offsetX: Dp = 6.dp,
    offsetY: Dp = 4.dp,
    corner: Dp = 4.dp,
    color: Color = Ink
): Modifier = drawBehind {
    val radius = corner.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(radius, radius)
    )
}

@Composable
fun NbButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .nbHardShadow()
            .height(56.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(3.dp, Ink),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = PrimaryInk,
            disabledContainerColor = Elevated,
            disabledContentColor = Muted
        )
    ) {
        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun NbButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Bone
) {
    Box(
        modifier = modifier
            .nbHardShadow()
            .height(52.dp)
            .background(if (enabled) containerColor else Elevated, RoundedCornerShape(4.dp))
            .border(BorderStroke(3.dp, if (enabled) Ink else Muted), RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.ExtraBold,
            color = if (enabled) Ink else Muted,
            fontSize = 16.sp,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun NbCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .nbHardShadow()
            .background(Card, RoundedCornerShape(4.dp))
            .border(BorderStroke(3.dp, Ink), RoundedCornerShape(4.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun NbFabScan(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .nbHardShadow(offsetX = 5.dp, offsetY = 4.dp)
            .background(Lime, RoundedCornerShape(4.dp))
            .border(BorderStroke(3.dp, Ink), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = "Scan",
            tint = Ink,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun BottomTabBar(
    selected: Int,
    onNavigate: ((com.example.nutrivision.ui.screens.Screen) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Card)
            .border(BorderStroke(3.dp, Ink)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabs = listOf(
            TabItem("INÍCIO", com.example.nutrivision.ui.screens.Screen.Home),
            TabItem("DIÁRIO", com.example.nutrivision.ui.screens.Screen.Diary),
            TabItem("PROGRESSO", com.example.nutrivision.ui.screens.Screen.Progress),
            TabItem("PERFIL", com.example.nutrivision.ui.screens.Screen.Profile)
        )
        tabs.forEachIndexed { index, item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(enabled = onNavigate != null) { onNavigate?.invoke(item.screen) }
            ) {
                Text(
                    text = item.label,
                    fontWeight = if (index == selected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = Ink,
                    fontSize = 11.sp,
                    letterSpacing = 1.1.sp
                )
                if (index == selected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.width(24.dp).height(2.dp).background(Ink))
                }
            }
        }
    }
}

data class TabItem(val label: String, val screen: com.example.nutrivision.ui.screens.Screen)

@Composable
fun NbSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            letterSpacing = (-0.3).sp
        )
        trailing?.invoke()
    }
}

@Composable
fun NbChip(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .nbHardShadow(
                offsetX = if (selected) 3.dp else 0.dp,
                offsetY = if (selected) 2.dp else 0.dp
            )
            .background(if (selected) Lime else Card, RoundedCornerShape(4.dp))
            .border(3.dp, Ink, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun NbProgressBar(
    progress: Float,
    color: Color = Primary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(Elevated, RoundedCornerShape(4.dp))
            .border(2.dp, Ink, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(color)
        )
    }
}

@Composable
fun NbStatCard(
    label: String,
    value: String,
    unit: String = "",
    valueColor: Color = Ink,
    modifier: Modifier = Modifier
) {
    NbCard(modifier = modifier) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Secondary,
            letterSpacing = 1.1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFamily,
                color = valueColor
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Secondary)
            }
        }
    }
}

@Composable
fun NbIconButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Card,
    size: Dp = 44.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .nbHardShadow(offsetX = 4.dp, offsetY = 3.dp)
            .background(containerColor, RoundedCornerShape(4.dp))
            .border(2.5.dp, Ink, RoundedCornerShape(4.dp))
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 16.sp)
    }
}

@Composable
fun NbMiniIconButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Card,
    size: Dp = 28.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .nbHardShadow(offsetX = 2.dp, offsetY = 2.dp)
            .background(containerColor, RoundedCornerShape(4.dp))
            .border(1.5.dp, Ink, RoundedCornerShape(4.dp))
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 13.sp)
    }
}
