package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.ui.components.NbButtonPrimary
import com.example.nutrivision.ui.components.NbButtonSecondary
import com.example.nutrivision.ui.components.NbCard
import com.example.nutrivision.ui.components.NbChip
import com.example.nutrivision.ui.components.NbSectionHeader
import com.example.nutrivision.ui.theme.*
import java.io.File
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    repository: RecordRepository,
    record: FoodRecord,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onCustomize: () -> Unit
) {
    var mealName by remember { mutableStateOf(record.mealName.ifEmpty { "Refeição" }) }
    var mealType by remember { mutableStateOf(record.mealType) }
    var kcalText by remember { mutableStateOf(record.calories.toDecimalText()) }
    var proteinText by remember { mutableStateOf(record.protein.toDecimalText()) }
    var carbsText by remember { mutableStateOf(record.carbs.toDecimalText()) }
    var fatText by remember { mutableStateOf(record.fat.toDecimalText()) }
    var pinnedMeals by remember { mutableStateOf(repository.loadPinnedMeals()) }

    val isManual = record.source == "manual"
    val kcal = kcalText.toDoubleValue()
    val protein = proteinText.toDoubleValue()
    val carbs = carbsText.toDoubleValue()
    val fat = fatText.toDoubleValue()
    val isPinned = pinnedMeals.any { it.matches(mealName, kcal) }
    val profile = remember { repository.loadUserProfile() }
    val alerts = remember(profile) { profile.healthAlerts() }
    val canSave = mealName.isNotBlank() && kcal > 0.0
    val meals = listOf("Café", "Almoço", "Jantar", "Lanche", "Outro")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bone)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        NbSectionHeader(title = "Resultado do scan")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isManual) {
                "Informe os valores da refeição e salve no diário"
            } else {
                "Confira os valores estimados e salve no diário"
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Secondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!record.imagePath.isNullOrBlank() && File(record.imagePath).exists()) {
            AsyncImage(
                model = File(record.imagePath),
                contentDescription = "Foto analisada",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(2.dp, Ink, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        NbCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CALORIAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Secondary, letterSpacing = 1.1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = kcal.toInt().toString(),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceMonoFamily,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Secondary, modifier = Modifier.padding(bottom = 5.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .background(
                            when (record.source) {
                                "gemini" -> Mint
                                "fixado" -> PinYellow
                                else -> Elevated
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, Ink, RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (record.source) {
                            "gemini" -> "GEMINI"
                            "manual" -> "MANUAL"
                            "fixado" -> "FIXADO"
                            else -> "LOCAL"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.0.sp,
                        color = Ink
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroCard("Prot.", "${protein.toInt()}g", ProteinInk, Modifier.weight(1f))
            MacroCard("Carb.", "${carbs.toInt()}g", CarbsInk, Modifier.weight(1f))
            MacroCard("Gord.", "${fat.toInt()}g", FatInk, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (alerts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "ATENÇÃO PARA VOCÊ", fontWeight = FontWeight.ExtraBold, color = DangerInk)
                Spacer(modifier = Modifier.height(6.dp))
                alerts.forEach { alert ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", fontWeight = FontWeight.ExtraBold, color = DangerInk, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = alert, fontWeight = FontWeight.Medium, color = Secondary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A foto não confirma ingredientes. Verifique o rótulo ou pergunte como o prato foi preparado.",
                    fontWeight = FontWeight.Medium,
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        NbSectionHeader(title = "Refeição")
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            meals.forEach { m ->
                NbChip(
                    text = m,
                    selected = mealType == m,
                    onClick = { mealType = m }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        NbSectionHeader(title = "Valores da refeição")
        Spacer(modifier = Modifier.height(12.dp))
        NbCard(modifier = Modifier.fillMaxWidth()) {
            if (!isManual) {
                Text(
                    text = "Valores estimados pela IA. Ajuste se necessário.",
                    fontWeight = FontWeight.Medium,
                    color = Secondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = mealName,
                onValueChange = { mealName = it },
                label = { Text("Nome da refeição", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = resultFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            ManualNumberField("Calorias (kcal)", kcalText) { kcalText = it }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ManualNumberField("Proteína (g)", proteinText, Modifier.weight(1f)) { proteinText = it }
                ManualNumberField("Carbo (g)", carbsText, Modifier.weight(1f)) { carbsText = it }
                ManualNumberField("Gordura (g)", fatText, Modifier.weight(1f)) { fatText = it }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NbButtonPrimary(
            text = "Salvar no diário",
            enabled = canSave,
            onClick = {
                repository.saveRecord(
                    record.copy(
                        mealName = mealName.ifBlank { "Refeição" },
                        mealType = mealType,
                        calories = kcal,
                        protein = protein,
                        carbs = carbs,
                        fat = fat
                    )
                )
                onSaved()
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (!canSave) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Informe um nome e calorias maiores que zero para salvar.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = WarningInk
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        NbButtonSecondary(
            text = if (isPinned) "Prato fixado ✓" else "Fixar prato",
            enabled = canSave,
            containerColor = if (isPinned) Lime else Bone,
            onClick = {
                repository.togglePinned(mealName, kcal, protein, carbs, fat, mealType, record.imagePath)
                pinnedMeals = repository.loadPinnedMeals()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isPinned) {
                "Este prato aparece na sua tela inicial e pode ser desfixado por lá."
            } else {
                "Fixe para adicionar este prato ao diário em um toque, sem escanear de novo."
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Muted
        )
        Spacer(modifier = Modifier.height(10.dp))
        NbButtonSecondary(
            text = "Personalizar prato",
            onClick = onCustomize,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        NbButtonSecondary(
            text = "Analisar outra foto",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MacroCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    NbCard(modifier = modifier) {
        Text(text = label.uppercase(), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Secondary, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceMonoFamily, color = color)
    }
}

@Composable
private fun ManualNumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onChange(input.filter { it.isDigit() || it == '.' || it == ',' }.take(7))
        },
        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(4.dp),
        colors = resultFieldColors()
    )
}

@Composable
private fun resultFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Card,
    unfocusedContainerColor = Card,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedLabelColor = PrimaryInk,
    unfocusedLabelColor = Secondary,
    focusedIndicatorColor = Ink,
    unfocusedIndicatorColor = Stroke,
    cursorColor = Ink
)

private fun Double.toDecimalText(): String =
    if (this <= 0.0) "" else String.format(Locale.ROOT, "%.1f", this).trimEnd('0').trimEnd('.')

private fun String.toDoubleValue(): Double =
    replace(',', '.').toDoubleOrNull()?.coerceIn(0.0, 5000.0) ?: 0.0
