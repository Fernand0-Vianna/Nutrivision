package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.PinnedMeal
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.ui.components.BottomTabBar
import com.example.nutrivision.ui.components.NbCard
import com.example.nutrivision.ui.components.NbChip
import com.example.nutrivision.ui.components.NbFabScan
import com.example.nutrivision.ui.components.NbMiniIconButton
import com.example.nutrivision.ui.components.NbProgressBar
import com.example.nutrivision.ui.components.nbHardShadow
import com.example.nutrivision.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    repository: RecordRepository,
    onScan: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    var refreshKey by remember { mutableStateOf(0) }
    val records = remember(refreshKey) { repository.recordsOfToday() }
    val stats = remember(refreshKey, records) { repository.stats(records) }
    val goals = remember(refreshKey) { repository.loadGoals() }
    var water by remember(refreshKey) { mutableStateOf(repository.loadWaterMl()) }
    val pinnedMeals = remember(refreshKey) { repository.loadPinnedMeals() }
    var addedPinnedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(addedPinnedId) {
        if (addedPinnedId != null) {
            delay(1600)
            addedPinnedId = null
        }
    }
    val profile = remember(refreshKey) { repository.loadUserProfile() }
    val firstName = profile.name.trim().split(" ").firstOrNull()?.takeIf { it.isNotBlank() }?.uppercase() ?: "USUÁRIO"
    val initials = profile.name.trim().split(Regex("\\s+")).take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "NV" }

    Box(modifier = Modifier.fillMaxSize().background(Bone)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OLÁ, $firstName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = ArchivoFamily,
                        color = Ink,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = todayLabel(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted,
                        letterSpacing = 0.4.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .nbHardShadow(offsetX = 4.dp, offsetY = 4.dp)
                        .background(Lime, RoundedCornerShape(4.dp))
                        .border(2.dp, Ink, RoundedCornerShape(4.dp))
                        .clickable { onNavigate(Screen.Profile) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = initials, fontWeight = FontWeight.ExtraBold, color = Ink)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "META DO DIA",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = ArchivoFamily,
                color = Ink,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "CALORIAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Secondary, letterSpacing = 1.1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stats.calories.toInt().toString(),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceMonoFamily,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/ ${goals.calories.toInt()} kcal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                NbProgressBar(
                    progress = if (goals.calories > 0) (stats.calories / goals.calories).toFloat() else 0f,
                    color = if (stats.calories > goals.calories) Danger else Primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MacroInline("P", stats.protein, goals.protein, ProteinInk)
                    MacroInline("C", stats.carbs, goals.carbs, CarbsInk)
                    MacroInline("G", stats.fat, goals.fat, FatInk)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÁGUA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = ArchivoFamily,
                    color = Ink,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "${formatMl(water)} / ${formatMl(goals.waterMl)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaterInk
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                NbProgressBar(
                    progress = if (goals.waterMl > 0) (water / goals.waterMl).toFloat() else 0f,
                    color = WaterFill
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NbChip(text = "+250 ml", onClick = { water = repository.addWaterMl(250.0) })
                    NbChip(text = "+500 ml", onClick = { water = repository.addWaterMl(500.0) })
                    Spacer(modifier = Modifier.weight(1f))
                    NbChip(
                        text = "Zerar",
                        onClick = {
                            repository.resetWater()
                            water = 0.0
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRATOS FIXADOS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = ArchivoFamily,
                    color = Ink,
                    letterSpacing = (-0.3).sp
                )
                if (pinnedMeals.isNotEmpty()) {
                    Text(
                        text = "${pinnedMeals.size} ${if (pinnedMeals.size == 1) "prato" else "pratos"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (pinnedMeals.isEmpty()) {
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "FIXE SEUS PRATOS FAVORITOS", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No resultado do scan, toque em FIXAR PRATO. Ele aparece aqui para adicionar ao dia em um toque.",
                        fontWeight = FontWeight.Medium,
                        color = Secondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pinnedMeals.forEach { meal ->
                        PinnedMealCard(
                            meal = meal,
                            added = addedPinnedId == meal.id,
                            onAdd = {
                                repository.addPinnedToToday(meal.id)
                                addedPinnedId = meal.id
                                refreshKey++
                            },
                            onUnpin = {
                                repository.deletePinnedMeal(meal.id)
                                refreshKey++
                            }
                        )
                    }
                    // Espaço para a sombra do último card não ser cortada pelo scroll.
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REFEIÇÕES DE HOJE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = ArchivoFamily,
                    color = Ink,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "${records.size} ${if (records.size == 1) "registro" else "registros"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Muted
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (records.isEmpty()) {
                NbCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onScan)
                ) {
                    Text(text = "ADICIONAR REFEIÇÃO", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use o botão SCAN para fotografar e analisar o alimento",
                        fontWeight = FontWeight.Medium,
                        color = Secondary
                    )
                }
            } else {
                records.sortedByDescending { it.timestamp }.forEach { r ->
                    MealCard(record = r)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                NbCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.Diary) }
                ) {
                    Text(
                        text = "VER DIÁRIO COMPLETO →",
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryInk,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(120.dp))
        }

        NbFabScan(
            onClick = onScan,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 92.dp)
        )
        BottomTabBar(
            selected = 0,
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PinnedMealCard(
    meal: PinnedMeal,
    added: Boolean,
    onAdd: () -> Unit,
    onUnpin: () -> Unit
) {
    val imagePath = meal.imagePath
    NbCard(modifier = Modifier.width(190.dp)) {
        if (!imagePath.isNullOrBlank() && File(imagePath).exists()) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Foto de ${meal.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(2.dp, Ink, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(PinYellow, RoundedCornerShape(4.dp))
                    .border(1.dp, Ink, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(text = "📌", fontSize = 12.sp)
            }
            NbMiniIconButton(label = "×", onClick = onUnpin)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = meal.name.uppercase(),
            fontWeight = FontWeight.ExtraBold,
            fontFamily = ArchivoFamily,
            color = Ink,
            fontSize = 14.sp,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${meal.calories.toInt()} kcal",
            fontWeight = FontWeight.Bold,
            fontFamily = SpaceMonoFamily,
            color = PrimaryInk,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "P:${meal.protein.toInt()}g C:${meal.carbs.toInt()}g G:${meal.fat.toInt()}g",
            fontWeight = FontWeight.Bold,
            color = Secondary,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .nbHardShadow(offsetX = 2.dp, offsetY = 2.dp)
                .background(if (added) Lime else Card, RoundedCornerShape(4.dp))
                .border(2.dp, Ink, RoundedCornerShape(4.dp))
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (added) "ADICIONADO ✓" else "+ ADICIONAR",
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                fontSize = 10.sp,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
private fun MacroInline(label: String, value: Double, goal: Double, color: Color) {
    Text(
        text = "$label ${value.toInt()}/${goal.toInt()}g",
        fontWeight = FontWeight.Bold,
        color = color,
        fontSize = 13.sp
    )
}

@Composable
private fun MealCard(record: FoodRecord) {
    val imagePath = record.imagePath
    NbCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!imagePath.isNullOrBlank() && File(imagePath).exists()) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "Foto de ${record.mealName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(2.dp, Ink, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.mealName.ifEmpty { "Refeição" },
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = ArchivoFamily,
                        color = Ink,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.ROOT).format(Date(record.timestamp)),
                        fontWeight = FontWeight.Bold,
                        color = Muted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${record.calories.toInt()} kcal · P:${record.protein.toInt()}g C:${record.carbs.toInt()}g G:${record.fat.toInt()}g",
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun todayLabel(): String {
    val fmt = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
    return fmt.format(Date()).replaceFirstChar { it.uppercase() }
}

private fun formatMl(ml: Double): String {
    return if (ml >= 1000) {
        String.format(Locale("pt", "BR"), "%.1f L", ml / 1000)
    } else {
        "${ml.toInt()} ml"
    }
}
