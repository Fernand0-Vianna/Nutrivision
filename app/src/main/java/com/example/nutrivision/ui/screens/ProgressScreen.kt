package com.example.nutrivision.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.data.WeightEntry
import com.example.nutrivision.ui.components.*
import com.example.nutrivision.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProgressScreen(
    repository: RecordRepository,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }
    val recordsAll = remember(refreshKey) { repository.loadRecords() }
    var range by remember { mutableStateOf("7 DIAS") }
    val rangeRecords = remember(range, recordsAll) {
        when (range) {
            "HOJE" -> repository.recordsOfToday()
            "7 DIAS" -> repository.recordsSince(7)
            "30 DIAS" -> repository.recordsSince(30)
            else -> recordsAll
        }
    }
    val stats = remember(refreshKey, rangeRecords) { repository.stats(rangeRecords) }
    val goals = remember(refreshKey) { repository.loadGoals() }
    val daily = remember(rangeRecords) { repository.recordsByDay(rangeRecords) }
    val weights = remember(refreshKey) { repository.loadWeights() }
    var exporting by remember { mutableStateOf(false) }

    // Metas são diárias: para 7/30 dias comparamos a média por dia com registro,
    // nunca a soma do período com uma única meta diária.
    val activeDays = remember(rangeRecords) { repository.daysWithRecords(rangeRecords).coerceAtLeast(1) }
    val caloriesPerDay = stats.calories / activeDays
    val proteinPerDay = stats.protein / activeDays
    val carbsPerDay = stats.carbs / activeDays
    val fatPerDay = stats.fat / activeDays

    Box(modifier = Modifier.fillMaxSize().background(Bone)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            NbSectionHeader(title = "Progresso e insights")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("HOJE", "7 DIAS", "30 DIAS", "TUDO").forEach { option ->
                    NbChip(text = option, selected = range == option, onClick = { range = option })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NbStatCard("Registros", stats.recordCount.toString(), "", Ink, Modifier.weight(1f))
                NbStatCard(
                    "Média/registro",
                    String.format(Locale.ROOT, "%.0f", if (stats.averageOfAverages.isNaN()) 0.0 else stats.averageOfAverages),
                    valueColor = PrimaryInk,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "EVOLUÇÃO DE PESO", fontWeight = FontWeight.ExtraBold, fontFamily = ArchivoFamily, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (weights.isEmpty()) "Nenhum peso registrado." else "${weights.size} registro(s)",
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (weights.size >= 2) {
                    WeightChart(weights = weights, modifier = Modifier.fillMaxWidth().height(160.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                weights.takeLast(4).asReversed().forEachIndexed { index, w ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(java.util.Date(w.timestamp)),
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "%.1f kg", w.kg),
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceMonoFamily,
                            color = PrimaryInk
                        )
                    }
                    if (index < minOf(weights.size - 1, 3)) Spacer(modifier = Modifier.height(2.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "MÉDIA DIÁRIA DE MACROS", fontWeight = FontWeight.ExtraBold, fontFamily = ArchivoFamily, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$activeDays dia(s) com registro no período",
                    fontWeight = FontWeight.Medium,
                    color = Muted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                MacroBar("Proteína", proteinPerDay, goals.protein, ProteinFill)
                Spacer(modifier = Modifier.height(10.dp))
                MacroBar("Carboidrato", carbsPerDay, goals.carbs, CarbsFill)
                Spacer(modifier = Modifier.height(10.dp))
                MacroBar("Gordura", fatPerDay, goals.fat, FatFill)
            }
            Spacer(modifier = Modifier.height(12.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "MÉDIA DIÁRIA VS META", fontWeight = FontWeight.ExtraBold, fontFamily = ArchivoFamily, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                NbProgressBar(
                    progress = if (goals.calories > 0) (caloriesPerDay / goals.calories).toFloat() else 0f,
                    color = if (caloriesPerDay > goals.calories) Danger else Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${caloriesPerDay.toInt()} de ${goals.calories.toInt()} kcal por dia • $activeDays dia(s) com registro",
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "HISTÓRICO POR DIA", fontWeight = FontWeight.ExtraBold, fontFamily = ArchivoFamily, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                if (daily.isEmpty()) {
                    Text(text = "Sem dados ainda.", fontWeight = FontWeight.Medium, color = Muted)
                } else {
                    daily.take(10).forEach { (day, list) ->
                        val dayAvg = if (list.isEmpty()) 0.0 else list.map { it.kcalPerRecord() }.average()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = day, fontWeight = FontWeight.ExtraBold, fontFamily = ArchivoFamily, color = Ink)
                            Text(
                                text = "${list.size} ref · ${String.format(Locale.ROOT, "%.1f", dayAvg)} · ${list.sumOf { it.calories }.toInt()} kcal",
                                fontWeight = FontWeight.Bold,
                                color = Secondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "INSIGHT", fontWeight = FontWeight.ExtraBold, fontFamily = ArchivoFamily, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val insight = when {
                    stats.recordCount == 0 -> "Registre sua primeira refeição para ver insights."
                    goals.protein > 0 && proteinPerDay < goals.protein / 2 -> "Sua proteína média está abaixo da meta. Considere incluir ovos, frango, peixe ou leguminosas."
                    goals.carbs > 0 && carbsPerDay > goals.carbs * 1.1 -> "Carboidrato médio acima da meta neste período. Ajuste porções se seu objetivo for redução."
                    goals.calories > 0 && caloriesPerDay > goals.calories -> "Você passou da meta calórica média por dia. Veja o diário para ajustar o próximo registro."
                    goals.protein > 0 && proteinPerDay >= goals.protein * 0.8 -> "Bom trabalho com a proteína! Mantenha o ritmo dos registros."
                    else -> "Bom balanço! Continue registrando para manter consistência e gerar insights mais precisos."
                }
                Text(text = insight, fontWeight = FontWeight.Medium, color = Secondary)
            }
            Spacer(modifier = Modifier.height(12.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "RELATÓRIO", fontWeight = FontWeight.ExtraBold, fontFamily = ArchivoFamily, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Exporte CSV com registros, médias, água e evolução de peso.",
                    fontWeight = FontWeight.Medium,
                    color = Secondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                NbButtonPrimary(
                    text = if (exporting) "Gerando..." else "Exportar relatório (.CSV)",
                    onClick = {
                        if (exporting) return@NbButtonPrimary
                        exporting = true
                        val file = repository.exportCsv()
                        exporting = false
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar relatório"))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
        BottomTabBar(
            selected = 2,
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MacroBar(label: String, value: Double, goal: Double, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label.uppercase(), fontWeight = FontWeight.Bold, color = Ink, fontSize = 11.sp, letterSpacing = 1.1.sp)
            Text(
                text = "${value.toInt()}g / ${goal.toInt()}g",
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        NbProgressBar(progress = if (goal > 0) (value / goal).toFloat() else 0f, color = color)
    }
}

@Composable
private fun WeightChart(weights: List<WeightEntry>, modifier: Modifier = Modifier) {
    if (weights.size < 2) return
    val points = weights.sortedBy { it.timestamp }
    val minW = points.minOf { it.kg }
    val maxW = points.maxOf { it.kg }
    val range = if (maxW - minW < 0.1) 1.0 else maxW - minW
    val ink = Ink
    val green = Primary

    Canvas(modifier = modifier) {
        val paddingX = 12.dp.toPx()
        val paddingY = 16.dp.toPx()
        val chartW = size.width - paddingX * 2
        val chartH = size.height - paddingY * 2
        val path = Path()
        points.forEachIndexed { index, entry ->
            val x = paddingX + (index.toFloat() / (points.size - 1).toFloat()) * chartW
            val y = paddingY + chartH - ((entry.kg - minW) / range).toFloat() * chartH
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = green, radius = 6.dp.toPx(), center = Offset(x, y))
            drawCircle(color = ink, radius = 6.dp.toPx(), center = Offset(x, y), style = Stroke(width = 3.dp.toPx()))
        }
        drawPath(
            path = path,
            color = green,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
