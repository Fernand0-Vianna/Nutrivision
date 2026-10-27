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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.ui.theme.*

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
            .shadow(4.dp, RoundedCornerShape(4.dp), ambientColor = Ink, spotColor = Ink)
            .height(56.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, Ink),
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(4.dp, RoundedCornerShape(4.dp), ambientColor = Ink, spotColor = Ink)
            .background(Bone, RoundedCornerShape(4.dp))
            .border(BorderStroke(2.dp, Ink), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
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
            .shadow(4.dp, RoundedCornerShape(4.dp), ambientColor = Ink, spotColor = Ink)
            .background(Card, RoundedCornerShape(4.dp))
            .border(BorderStroke(2.dp, Ink), RoundedCornerShape(4.dp))
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
            .shadow(4.dp, RoundedCornerShape(4.dp), ambientColor = Ink, spotColor = Ink)
            .background(Lime, RoundedCornerShape(4.dp))
            .border(BorderStroke(3.dp, Ink), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "SCAN", fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 10.sp, letterSpacing = 0.5.sp)
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
            .shadow(if (selected) 4.dp else 0.dp, RoundedCornerShape(4.dp), ambientColor = Ink, spotColor = Ink)
            .background(if (selected) Lime else Card, RoundedCornerShape(4.dp))
            .border(2.dp, Ink, RoundedCornerShape(4.dp))
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(4.dp, RoundedCornerShape(4.dp), ambientColor = Ink, spotColor = Ink)
            .background(Card, RoundedCornerShape(4.dp))
            .border(2.dp, Ink, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 16.sp)
    }
}
