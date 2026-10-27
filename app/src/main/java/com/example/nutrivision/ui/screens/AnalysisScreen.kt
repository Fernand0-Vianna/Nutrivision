package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.ui.components.NbButtonPrimary
import com.example.nutrivision.ui.components.NbCard
import com.example.nutrivision.ui.components.NbProgressBar
import com.example.nutrivision.ui.components.NbStatCard
import com.example.nutrivision.ui.theme.*
import java.util.Locale

@Composable
fun AnalysisScreen(
    repository: RecordRepository,
    onBack: () -> Unit
) {
    val today = remember { repository.recordsOfToday() }
    val stats = remember { repository.stats(today) }
    val goals = remember { repository.loadGoals() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bone)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "ANÁLISE NUTRICIONAL", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NbStatCard("Registros", stats.recordCount.toString(), "", Ink, Modifier.weight(1f))
            NbStatCard(
                "Média",
                String.format(Locale.ROOT, "%.1f", stats.averageOfAverages),
                "",
                PrimaryInk,
                Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        NbCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "INSIGHT NUTRICIONAL", fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    stats.recordCount == 0 -> "Sem dados suficientes. Registre refeições para gerar uma análise."
                    stats.protein < goals.protein / 2 -> "Boa quantidade de carboidrato, mas a proteína está abaixo do ideal. Inclua uma fonte proteica."
                    stats.fat > goals.fat -> "A gordura passou da meta. Prefira preparos grelhados ou assados."
                    else -> "Balanço equilibrado de macros. Continue registrando para manter a consistência."
                },
                fontWeight = FontWeight.Medium,
                color = Secondary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        NbCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "BALANÇO VS METAS", fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            BalanceRow("Calorias", stats.calories, goals.calories, Primary)
            Spacer(modifier = Modifier.height(10.dp))
            BalanceRow("Proteína", stats.protein, goals.protein, ProteinFill)
            Spacer(modifier = Modifier.height(10.dp))
            BalanceRow("Carboidrato", stats.carbs, goals.carbs, CarbsFill)
            Spacer(modifier = Modifier.height(10.dp))
            BalanceRow("Gordura", stats.fat, goals.fat, FatFill)
        }
        Spacer(modifier = Modifier.height(24.dp))
        NbButtonPrimary(text = "Voltar", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun BalanceRow(
    label: String,
    value: Double,
    goal: Double,
    color: androidx.compose.ui.graphics.Color
) {
    val ratio = if (goal > 0) (value / goal).toFloat() else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label.uppercase(), fontWeight = FontWeight.Bold, color = Ink, fontSize = 11.sp, letterSpacing = 1.1.sp)
            Text(
                text = "${value.toInt()} / ${goal.toInt()}",
                fontWeight = FontWeight.Bold,
                color = if (ratio > 1f) DangerInk else Secondary,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        NbProgressBar(progress = ratio, color = if (ratio > 1f) Danger else color)
    }
}
