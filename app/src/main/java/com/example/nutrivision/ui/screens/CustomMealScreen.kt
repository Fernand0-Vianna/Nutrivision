package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.PinnedMeal
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.ui.components.NbButtonPrimary
import com.example.nutrivision.ui.components.NbButtonSecondary
import com.example.nutrivision.ui.components.NbCard
import com.example.nutrivision.ui.components.NbChip
import com.example.nutrivision.ui.components.NbIconButton
import com.example.nutrivision.ui.components.NbMiniIconButton
import com.example.nutrivision.ui.theme.ArchivoFamily
import com.example.nutrivision.ui.theme.Bone
import com.example.nutrivision.ui.theme.CarbsInk
import com.example.nutrivision.ui.theme.FatInk
import com.example.nutrivision.ui.theme.Ink
import com.example.nutrivision.ui.theme.Lime
import com.example.nutrivision.ui.theme.Muted
import com.example.nutrivision.ui.theme.PrimaryInk
import com.example.nutrivision.ui.theme.ProteinInk
import com.example.nutrivision.ui.theme.Secondary
import com.example.nutrivision.ui.theme.SpaceMonoFamily
import com.example.nutrivision.ui.theme.Stroke
import java.util.Locale

/**
 * Personalização de pratos: monta um prato com nome, refeição e macros,
 * adiciona ao diário ou guarda na tela inicial (prato personalizado).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomMealScreen(
    repository: RecordRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var kcalText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Café") }
    var pinned by remember { mutableStateOf(repository.loadPinnedMeals()) }
    var pinnedFeedback by remember { mutableStateOf(false) }

    val kcal = kcalText.toNvDouble()
    val protein = proteinText.toNvDouble()
    val carbs = carbsText.toNvDouble()
    val fat = fatText.toNvDouble()
    val canSave = name.isNotBlank() && kcal > 0.0
    val meals = listOf("Café", "Almoço", "Jantar", "Lanche", "Outro")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bone)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NbIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "PERSONALIZAR PRATO",
                fontFamily = ArchivoFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Ink,
                letterSpacing = (-0.3).sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        NbCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "PRÉVIA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Secondary, letterSpacing = 1.1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name.ifBlank { "Meu prato" }.uppercase(),
                        fontFamily = ArchivoFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = kcal.toInt().toString(),
                        fontFamily = SpaceMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Secondary, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "P ${protein.toInt()}g", fontWeight = FontWeight.Bold, color = ProteinInk, fontSize = 13.sp)
                Text(text = "C ${carbs.toInt()}g", fontWeight = FontWeight.Bold, color = CarbsInk, fontSize = 13.sp)
                Text(text = "G ${fat.toInt()}g", fontWeight = FontWeight.Bold, color = FatInk, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "DADOS DO PRATO", fontFamily = ArchivoFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Ink)
        Spacer(modifier = Modifier.height(12.dp))
        NbCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; pinnedFeedback = false },
                label = { Text("Nome do prato", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = customFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            NumberField("Calorias (kcal)", kcalText) { kcalText = it; pinnedFeedback = false }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Proteína (g)", proteinText, Modifier.weight(1f)) { proteinText = it; pinnedFeedback = false }
                NumberField("Carbo (g)", carbsText, Modifier.weight(1f)) { carbsText = it; pinnedFeedback = false }
                NumberField("Gordura (g)", fatText, Modifier.weight(1f)) { fatText = it; pinnedFeedback = false }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "REFEIÇÃO", fontFamily = ArchivoFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Ink)
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            meals.forEach { m ->
                NbChip(text = m, selected = mealType == m, onClick = { mealType = m })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NbButtonPrimary(
            text = "Salvar no diário",
            enabled = canSave,
            onClick = {
                repository.saveRecord(
                    FoodRecord(
                        mealName = name.trim(),
                        mealType = mealType,
                        calories = kcal,
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                        source = "manual"
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
                color = com.example.nutrivision.ui.theme.WarningInk
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        NbButtonSecondary(
            text = if (pinnedFeedback) "Salvo na tela inicial ✓" else "Salvar na tela inicial",
            enabled = canSave,
            containerColor = if (pinnedFeedback) Lime else Bone,
            onClick = {
                repository.saveCustomPinnedMeal(
                    name = name.trim(),
                    calories = kcal,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    mealType = mealType
                )
                pinned = repository.loadPinnedMeals()
                pinnedFeedback = true
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Pratos personalizados aparecem na tela inicial e podem ser adicionados ao dia em um toque.",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Muted
        )

        if (pinned.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Tune, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "MEUS PRATOS PERSONALIZADOS", fontFamily = ArchivoFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Ink)
            }
            Spacer(modifier = Modifier.height(12.dp))
            pinned.forEach { meal ->
                PinnedMealTile(meal = meal, onDelete = {
                    repository.deletePinnedMeal(meal.id)
                    pinned = repository.loadPinnedMeals()
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NbButtonSecondary(text = "Voltar", onClick = onBack, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PinnedMealTile(meal: PinnedMeal, onDelete: () -> Unit) {
    NbCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Lime, RoundedCornerShape(4.dp))
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Restaurant, contentDescription = null, tint = Ink, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name.uppercase(),
                    fontFamily = ArchivoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${meal.calories.toInt()} kcal · P:${meal.protein.toInt()}g C:${meal.carbs.toInt()}g G:${meal.fat.toInt()}g",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Secondary
                )
            }
            NbMiniIconButton(
                onClick = onDelete,
                icon = Icons.Filled.Close,
                size = 34.dp,
                contentDescription = "Remover ${meal.name}"
            )
        }
    }
}

@Composable
private fun NumberField(
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
        colors = customFieldColors()
    )
}

@Composable
private fun customFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = com.example.nutrivision.ui.theme.Card,
    unfocusedContainerColor = com.example.nutrivision.ui.theme.Card,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedLabelColor = PrimaryInk,
    unfocusedLabelColor = Secondary,
    focusedIndicatorColor = Ink,
    unfocusedIndicatorColor = Stroke,
    cursorColor = Ink
)

private fun String.toNvDouble(): Double =
    replace(',', '.').toDoubleOrNull()?.coerceIn(0.0, 5000.0) ?: 0.0
