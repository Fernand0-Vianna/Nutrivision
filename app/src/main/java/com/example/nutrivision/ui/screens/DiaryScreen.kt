package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.ui.components.*
import com.example.nutrivision.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(
    repository: RecordRepository,
    onNavigate: (Screen) -> Unit
) {
    var refreshKey by remember { mutableStateOf(0) }
    val records = remember(refreshKey) { repository.loadRecords() }
    var filter by remember { mutableStateOf("TODOS") }
    var range by remember { mutableStateOf("TUDO") }
    var pendingDelete by remember { mutableStateOf<FoodRecord?>(null) }
    val pinnedMeals = remember(refreshKey) { repository.loadPinnedMeals() }

    val inRange = remember(range, records) {
        when (range) {
            "HOJE" -> repository.recordsOfToday()
            "7 DIAS" -> repository.recordsSince(7)
            "30 DIAS" -> repository.recordsSince(30)
            else -> records
        }
    }
    val filtered = remember(filter, inRange) {
        if (filter == "TODOS") inRange
        else inRange.filter { it.mealName.equals(filter, ignoreCase = true) }
    }
    val grouped = remember(filtered) { repository.recordsByDay(filtered) }

    Box(modifier = Modifier.fillMaxSize().background(Bone)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            NbSectionHeader(title = "Diário alimentar")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${filtered.size} ${if (filtered.size == 1) "refeição" else "refeições"} no período · ${filtered.sumOf { it.calories }.toInt()} kcal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Secondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NbStatCard("Registros", filtered.size.toString(), "", Ink, Modifier.weight(1f))
                NbStatCard(
                    "Média/registro",
                    String.format(Locale.ROOT, "%.0f", if (filtered.isEmpty()) 0.0 else filtered.map { it.average() }.average()),
                    valueColor = PrimaryInk,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("HOJE", "7 DIAS", "30 DIAS", "TUDO").forEach { option ->
                    NbChip(
                        text = option,
                        selected = range == option,
                        onClick = { range = option },
                        modifier = Modifier.testTag("range_$option")
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TODOS", "Café", "Almoço", "Jantar", "Lanche", "Outro").forEach { option ->
                    NbChip(
                        text = option,
                        selected = filter == option,
                        onClick = { filter = option },
                        modifier = Modifier.testTag("meal_${option.uppercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (filtered.isEmpty()) {
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (records.isEmpty()) "NENHUM REGISTRO AINDA" else "NADA NESTE FILTRO",
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use o botão SCAN para analisar um alimento",
                        fontWeight = FontWeight.Medium,
                        color = Secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    grouped.forEach { (day, items) ->
                        item(key = "header_$day") {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day.uppercase(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = ArchivoFamily,
                                    color = Ink,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${items.size} ref · ${items.sumOf { it.calories }.toInt()} kcal",
                                    fontWeight = FontWeight.Bold,
                                    color = Muted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        items(items, key = { it.id }) { r ->
                            DiaryRow(
                                record = r,
                                isPinned = pinnedMeals.any { it.matches(r.mealName, r.calories) },
                                onPin = {
                                    repository.togglePinned(
                                        r.mealName,
                                        r.calories,
                                        r.protein,
                                        r.carbs,
                                        r.fat
                                    )
                                    refreshKey++
                                },
                                onDelete = { pendingDelete = r }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        BottomTabBar(
            selected = 1,
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        pendingDelete?.let { record ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text(text = "Excluir refeição?", fontWeight = FontWeight.ExtraBold, color = Ink) },
                text = {
                    Text(
                        text = "O registro de ${record.mealName.ifEmpty { "refeição" }} e a foto serão apagados. Esta ação não pode ser desfeita.",
                        fontWeight = FontWeight.Medium,
                        color = Secondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        repository.deleteRecord(record.id)
                        refreshKey++
                        pendingDelete = null
                    }) {
                        Text(text = "EXCLUIR", fontWeight = FontWeight.ExtraBold, color = DangerInk)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) {
                        Text(text = "CANCELAR", fontWeight = FontWeight.ExtraBold, color = Ink)
                    }
                },
                containerColor = Card,
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Composable
private fun DiaryRow(
    record: FoodRecord,
    isPinned: Boolean,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    NbCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!record.imagePath.isNullOrBlank() && File(record.imagePath).exists()) {
                AsyncImage(
                    model = File(record.imagePath),
                    contentDescription = "Foto da refeição",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
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
                        text = record.mealName.ifEmpty { "Refeição" }.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = ArchivoFamily,
                        color = Ink,
                        fontSize = 15.sp
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
                    text = "${record.calories.toInt()} kcal · média ${String.format(Locale.ROOT, "%.1f", record.average())}",
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "P:${record.protein.toInt()}g · C:${record.carbs.toInt()}g · G:${record.fat.toInt()}g · ${when (record.source) {
                        "gemini" -> "IA"
                        "manual" -> "MANUAL"
                        "fixado" -> "FIXADO"
                        else -> "REGISTRO"
                    }}",
                    fontWeight = FontWeight.Bold,
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            NbIconButton(
                label = if (isPinned) "✓" else "📌",
                onClick = onPin,
                containerColor = if (isPinned) Lime else Card,
                size = 40.dp
            )
            NbIconButton(label = "×", onClick = onDelete, size = 40.dp)
        }
    }
}
