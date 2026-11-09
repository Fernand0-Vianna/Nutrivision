package com.example.nutrivision.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.data.ApiConfig
import com.example.nutrivision.data.Goals
import com.example.nutrivision.data.ProfileOptions
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.data.UserProfile
import com.example.nutrivision.ui.components.*
import com.example.nutrivision.ui.theme.*
import java.util.Locale

@Composable
fun ProfileScreen(
    repository: RecordRepository,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    var goals by remember { mutableStateOf(repository.loadGoals()) }
    var saved by remember { mutableStateOf(false) }
    val statsAll = remember { repository.stats() }
    val mediaGeral = remember { repository.mediaGeral() }
    var waterRefresh by remember { mutableStateOf(0) }
    var water by remember(waterRefresh) { mutableStateOf(repository.loadWaterMl()) }
    var weightInput by remember { mutableStateOf(repository.currentWeight()?.kg?.let { String.format(Locale("pt", "BR"), "%.1f", it) } ?: "") }
    var weightSaved by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var apiKey by remember { mutableStateOf(ApiConfig.resolveKey(context)) }
    var showKey by remember { mutableStateOf(false) }
    var keySaved by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf(repository.loadUserProfile()) }

    Box(modifier = Modifier.fillMaxSize().background(Bone)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .nbHardShadow(offsetX = 5.dp, offsetY = 4.dp)
                        .background(Lime, RoundedCornerShape(4.dp))
                        .border(3.dp, Ink, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = userProfile.name.split("\\s+".toRegex()).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
                    Text(text = if (initials.isEmpty()) "NV" else initials, fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userProfile.name.isBlank()) "USUÁRIO" else userProfile.name.trim().uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = ArchivoFamily,
                        color = Ink,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(text = "Nutrivision • IMC, saúde e personalização", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Secondary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "SEU IMC", fontWeight = FontWeight.ExtraBold, color = Ink)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = userProfile.bmiFormatted(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ArchivoFamily,
                        color = Ink
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = userProfile.bmiCategory(), fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text(
                            text = if (userProfile.heightCm > 0 && userProfile.weightKg > 0) "${userProfile.weightKg.toInt()} kg • ${userProfile.heightCm.toInt()} cm" else "Complete o onboarding para calcular",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Muted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val hints = listOf("Abaixo do peso" to "< 18,5", "Peso normal" to "18,5 - 24,9", "Sobrepeso" to "25 - 29,9", "Obesidade" to ">= 30")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    hints.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowItems.forEach { (label, range) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Elevated, RoundedCornerShape(4.dp))
                                        .border(1.dp, Ink, RoundedCornerShape(4.dp))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                                        Text(text = range, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "RESTRIÇÕES E SAÚDE", fontWeight = FontWeight.ExtraBold, color = Ink)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Objetivo: ${ProfileOptions.goalLabel(userProfile.goal)} • Atividade: ${ProfileOptions.activityLabel(userProfile.activityLevel)}", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                if (userProfile.intolerances.isNotEmpty()) {
                    Text(text = "Intolerâncias: ${ProfileOptions.labels(userProfile.intolerances)}", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                }
                if (userProfile.allergies.isNotEmpty()) {
                    Text(text = "Alergias: ${ProfileOptions.labels(userProfile.allergies)}", fontWeight = FontWeight.Medium, color = DangerInk, fontSize = 13.sp)
                }
                if (userProfile.conditions.isNotEmpty()) {
                    Text(text = "Condições: ${ProfileOptions.labels(userProfile.conditions)}", fontWeight = FontWeight.Medium, color = WarningInk, fontSize = 13.sp)
                }
                if (userProfile.intolerances.isEmpty() && userProfile.allergies.isEmpty() && userProfile.conditions.isEmpty()) {
                    Text(text = "Nenhuma restrição cadastrada.", fontWeight = FontWeight.Medium, color = Muted, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                NbButtonPrimary(
                    text = "Editar perfil e restrições",
                    onClick = { onNavigate(Screen.Onboarding) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (userProfile.healthAlerts().isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "AVISOS DE SAÚDE", fontWeight = FontWeight.ExtraBold, color = DangerInk)
                    Spacer(modifier = Modifier.height(8.dp))
                    userProfile.healthAlerts().forEach { alert ->
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
                        text = "Avisos informativos. Não substituem avaliação médica ou nutricional.",
                        fontWeight = FontWeight.Medium,
                        color = Muted,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            NbSectionHeader(title = "Metas diárias")
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                GoalField("Calorias (kcal)", goals.calories) { goals = goals.copy(calories = it) }
                Spacer(modifier = Modifier.height(10.dp))
                GoalField("Proteína (g)", goals.protein) { goals = goals.copy(protein = it) }
                Spacer(modifier = Modifier.height(10.dp))
                GoalField("Carboidrato (g)", goals.carbs) { goals = goals.copy(carbs = it) }
                Spacer(modifier = Modifier.height(10.dp))
                GoalField("Gordura (g)", goals.fat) { goals = goals.copy(fat = it) }
                Spacer(modifier = Modifier.height(10.dp))
                GoalField("Água (ml)", goals.waterMl) { goals = goals.copy(waterMl = it) }
                Spacer(modifier = Modifier.height(16.dp))
                NbButtonPrimary(
                    text = if (saved) "Salvo!" else "Salvar metas",
                    onClick = {
                        repository.saveGoals(goals)
                        saved = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            NbSectionHeader(title = "Evolução de peso")
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "REGISTRAR PESO ATUAL", fontWeight = FontWeight.ExtraBold, color = Ink)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it.replace(',', '.') },
                        label = { Text("Peso (kg)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        colors = fieldColors()
                    )
                    NbButtonPrimary(
                        text = if (weightSaved) "OK" else "Salvar",
                        onClick = {
                            val kg = weightInput.replace(',', '.').toDoubleOrNull()
                            if (kg == null || kg < 20.0 || kg > 500.0) {
                                weightError = "Informe um peso entre 20 e 500 kg."
                            } else {
                                repository.saveWeight(kg)
                                userProfile = repository.loadUserProfile()
                                weightError = null
                                weightSaved = true
                                weightInput = String.format(Locale("pt", "BR"), "%.1f", kg)
                            }
                        },
                        modifier = Modifier.width(110.dp)
                    )
                }
                weightError?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = it, fontWeight = FontWeight.Bold, color = DangerInk, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Registrado por dia; novos valores substituem o peso do mesmo dia.",
                    fontWeight = FontWeight.Medium,
                    color = Muted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            NbSectionHeader(title = "Integração com a IA")
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "GOOGLE GEMINI (VISÃO)", fontWeight = FontWeight.ExtraBold, color = Ink)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (ApiConfig.isConfigured(context)) "Chave configurada neste aparelho." else "Sem chave: use a análise manual no scan.",
                    fontWeight = FontWeight.Bold,
                    color = if (ApiConfig.isConfigured(context)) PrimaryInk else WarningInk,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it; keySaved = false },
                    label = { Text("Gemini API Key", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    shape = RoundedCornerShape(4.dp),
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NbIconButton(
                        onClick = { showKey = !showKey },
                        icon = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showKey) "Ocultar chave Gemini" else "Mostrar chave Gemini"
                    )
                    NbButtonPrimary(
                        text = if (keySaved) "Salvo!" else "Salvar chave",
                        onClick = {
                            ApiConfig.saveKey(context, apiKey)
                            keySaved = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    NbButtonPrimary(
                        text = "Limpar",
                        onClick = {
                            ApiConfig.clearKey(context)
                            apiKey = ""
                            keySaved = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                NbButtonPrimary(
                    text = "Obter chave no Google AI Studio",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            NbSectionHeader(title = "Resumo")
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NbStatCard("Registros", statsAll.recordCount.toString(), "", Ink, Modifier.weight(1f))
                NbStatCard(
                    "Média/registro",
                    String.format(Locale.ROOT, "%.0f", if (mediaGeral.isNaN()) 0.0 else mediaGeral),
                    "",
                    PrimaryInk,
                    Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "CONTROLE DE ÁGUA", fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                NbProgressBar(progress = if (goals.waterMl > 0) (water / goals.waterMl).toFloat() else 0f, color = WaterFill)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${if (water >= 1000) String.format(Locale("pt", "BR"), "%.1f L", water / 1000) else "${water.toInt()} ml"} de ${if (goals.waterMl >= 1000) String.format(Locale("pt", "BR"), "%.1f L", goals.waterMl / 1000) else "${goals.waterMl.toInt()} ml"}",
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NbIconButton(
                        label = "+250",
                        onClick = { water = repository.addWaterMl(250.0) },
                        contentDescription = "Adicionar 250 ml de água"
                    )
                    NbIconButton(
                        label = "+500",
                        onClick = { water = repository.addWaterMl(500.0) },
                        contentDescription = "Adicionar 500 ml de água"
                    )
                    NbIconButton(
                        onClick = { repository.resetWater(); water = 0.0 },
                        icon = Icons.Filled.Refresh,
                        contentDescription = "Zerar água do dia"
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            NbCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "SOBRE", fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Nutrivision 1.2.0", fontWeight = FontWeight.Bold, color = Secondary, fontSize = 13.sp)
                Text(text = "Análise de alimentos por imagem • Diário • Progresso • Peso • Água", fontWeight = FontWeight.Medium, color = Muted, fontSize = 13.sp)
                Text(text = "Dados em .relatorio/ e fotos em armazenamento privado do app", fontWeight = FontWeight.Medium, color = Muted, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
        BottomTabBar(
            selected = 3,
            onNavigate = onNavigate,
            onScan = { onNavigate(Screen.Scan) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun GoalField(label: String, value: Double, onChange: (Double) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toInt().toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it.filter { c -> c.isDigit() }
            text.toDoubleOrNull()?.let(onChange)
        },
        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(4.dp),
        colors = fieldColors()
    )
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
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
