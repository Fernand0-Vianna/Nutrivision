package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.data.ProfileOptions
import com.example.nutrivision.data.UserProfile
import com.example.nutrivision.ui.components.NbButtonPrimary
import com.example.nutrivision.ui.components.NbCard
import com.example.nutrivision.ui.components.NbChip
import com.example.nutrivision.ui.components.NbSectionHeader
import com.example.nutrivision.ui.components.nbHardShadow
import com.example.nutrivision.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initialProfile: UserProfile,
    onComplete: (UserProfile) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf(initialProfile.name) }
    var age by remember { mutableStateOf(initialProfile.age?.toString() ?: "") }
    var sex by remember { mutableStateOf(initialProfile.sex.ifEmpty { "M" }) }
    var height by remember { mutableStateOf(if (initialProfile.heightCm > 0) initialProfile.heightCm.toInt().toString() else "") }
    var weight by remember { mutableStateOf(if (initialProfile.weightKg > 0) String.format(Locale("pt", "BR"), "%.1f", initialProfile.weightKg) else "") }
    var activityLevel by remember { mutableStateOf(initialProfile.activityLevel) }
    var goal by remember { mutableStateOf(initialProfile.goal) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val intolerances = remember { mutableStateListOf<String>() }
    val allergies = remember { mutableStateListOf<String>() }
    val conditions = remember { mutableStateListOf<String>() }

    LaunchedEffect(initialProfile) {
        intolerances.clear()
        intolerances.addAll(initialProfile.intolerances)
        allergies.clear()
        allergies.addAll(initialProfile.allergies)
        conditions.clear()
        conditions.addAll(initialProfile.conditions)
    }

    val h = height.toDoubleOrNull() ?: 0.0
    val w = weight.replace(',', '.').toDoubleOrNull() ?: 0.0
    val bmi = if (h > 0 && w > 0) w / ((h / 100.0) * (h / 100.0)) else 0.0
    val bmiCat = when {
        bmi <= 0.0 -> "Não calculado"
        bmi < 18.5 -> "Abaixo do peso"
        bmi < 25.0 -> "Peso normal"
        bmi < 30.0 -> "Sobrepeso"
        bmi < 35.0 -> "Obesidade grau I"
        bmi < 40.0 -> "Obesidade grau II"
        else -> "Obesidade grau III"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bone)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        NbSectionHeader(title = if (initialProfile.completed) "ATUALIZAR PERFIL" else "CONFIGURAR PERFIL")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Passo ${step + 1} de 4 • IMC e preferências para personalizar o app",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Secondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (step) {
            0 -> {
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "DADOS PESSOAIS", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(modifier = Modifier.height(10.dp))
                    OnboardField("Nome", name) { name = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        OnboardField("Idade", age, Modifier.weight(0.8f)) { age = it.filter { c -> c.isDigit() } }
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(text = "Sexo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Secondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("M" to "Masculino", "F" to "Feminino", "OUTRO" to "Outro").forEach { (key, label) ->
                                    NbChip(text = label, selected = sex == key, onClick = { sex = key })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OnboardField("Altura (cm)", height, Modifier.weight(1f)) { height = it.filter { c -> c.isDigit() } }
                        OnboardField("Peso (kg)", weight, Modifier.weight(1f)) { weight = it.replace(',', '.') }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    NbCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "CÁLCULO DE IMC", fontWeight = FontWeight.ExtraBold, color = Ink)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (bmi <= 0.0) "--" else String.format(Locale("pt", "BR"), "%.1f", bmi),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = ArchivoFamily,
                                color = Ink
                            )
                            Text(text = bmiCat, fontWeight = FontWeight.Bold, color = Secondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "IMC = peso (kg) / (altura (m))²",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Muted
                        )
                    }
                }
            }
            1 -> {
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "OBJETIVO E ATIVIDADE", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Objetivo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PERDER" to "Perder peso", "MANTER" to "Manter peso", "GANHAR" to "Ganhar massa").forEach { (key, label) ->
                            NbChip(text = label, selected = goal == key, onClick = { goal = key })
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Nível de atividade", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "SEDENTARIO" to "Sedentário (pouco ou nenhum exercício)",
                            "LEVE" to "Leve (1-3x/semana)",
                            "MODERADO" to "Moderado (3-5x/semana)",
                            "ATIVO" to "Ativo (6-7x/semana)",
                            "MUITO_ATIVO" to "Muito ativo (exercício intenso/diário)"
                        ).forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .nbHardShadow(
                                        offsetX = if (activityLevel == key) 3.dp else 0.dp,
                                        offsetY = if (activityLevel == key) 2.dp else 0.dp
                                    )
                                    .background(if (activityLevel == key) Mint else Card, RoundedCornerShape(4.dp))
                                    .border(2.dp, Ink, RoundedCornerShape(4.dp))
                                    .clickable { activityLevel = key }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = label, fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            2 -> {
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "INTOLERÂNCIAS ALIMENTARES", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Vamos te avisar caso o alimento possa conter algo sensível.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Muted)
                    Spacer(modifier = Modifier.height(10.dp))
                    ChipFlow(ProfileOptions.intolerances, intolerances)
                }
                Spacer(modifier = Modifier.height(12.dp))
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "ALERGIAS", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Avisos mais fortes para itens com maior risco.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Muted)
                    Spacer(modifier = Modifier.height(10.dp))
                    ChipFlow(ProfileOptions.allergies, allergies)
                }
            }
            3 -> {
                NbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "CONDIÇÕES DE SAÚDE", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Usamos isso para dar avisos e personalizar sugestões.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Muted)
                    Spacer(modifier = Modifier.height(10.dp))
                    val list = ProfileOptions.conditions
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        list.forEach { (key, item) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .nbHardShadow(
                                        offsetX = if (conditions.contains(key)) 3.dp else 0.dp,
                                        offsetY = if (conditions.contains(key)) 2.dp else 0.dp
                                    )
                                    .background(if (conditions.contains(key)) Soft else Card, RoundedCornerShape(4.dp))
                                    .border(2.dp, Ink, RoundedCornerShape(4.dp))
                                    .clickable {
                                        if (conditions.contains(key)) conditions.remove(key) else conditions.add(key)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    NbCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "RESUMO DO SEU PERFIL", fontWeight = FontWeight.ExtraBold, color = Ink)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Nome: ${name.trim().ifEmpty { "-" }} • Idade: ${age.ifEmpty { "-" }} • Sexo: ${ProfileOptions.sexLabel(sex)}", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                        Text(text = "Altura/Peso: ${if (height.isEmpty()) "-" else "${height} cm"} / ${if (weight.isEmpty()) "-" else "${weight} kg"} • IMC: ${if (bmi <= 0.0) "--" else String.format(Locale("pt", "BR"), "%.1f", bmi)} ($bmiCat)", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                        Text(text = "Objetivo: ${ProfileOptions.goalLabel(goal)} • Atividade: ${ProfileOptions.activityLabel(activityLevel)}", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                        Text(text = "Intolerâncias: ${if (intolerances.isEmpty()) "Nenhuma" else ProfileOptions.labels(intolerances)}", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                        Text(text = "Alergias: ${if (allergies.isEmpty()) "Nenhuma" else ProfileOptions.labels(allergies)}", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                        Text(text = "Condições: ${if (conditions.isEmpty()) "Nenhuma" else ProfileOptions.labels(conditions)}", fontWeight = FontWeight.Medium, color = Secondary, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        if (validationError != null) {
            Text(
                text = validationError ?: "",
                fontWeight = FontWeight.Bold,
                color = DangerInk,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) {
                NbButtonPrimary(
                    text = "Voltar",
                    onClick = { validationError = null; step-- },
                    modifier = Modifier.weight(1f)
                )
            }
            NbButtonPrimary(
                text = if (step == 3) "Salvar e continuar" else "Continuar",
                onClick = {
                    if (step == 0) {
                        val ageValue = age.toIntOrNull()
                        val hValue = height.toDoubleOrNull()
                        val wValue = weight.replace(',', '.').toDoubleOrNull()
                        validationError = when {
                            name.isBlank() -> "Informe seu nome para continuar."
                            ageValue == null || ageValue !in 10..120 -> "Informe uma idade válida (10 a 120 anos)."
                            hValue == null || hValue !in 100.0..250.0 -> "Informe uma altura válida (100 a 250 cm)."
                            wValue == null || wValue !in 30.0..350.0 -> "Informe um peso válido (30 a 350 kg)."
                            else -> null
                        }
                    }
                    if (validationError == null) {
                        if (step < 3) {
                            step++
                        } else {
                            val profile = UserProfile(
                                name = name.trim().ifEmpty { initialProfile.name.trim() },
                                age = age.toIntOrNull(),
                                sex = sex,
                                heightCm = height.toDoubleOrNull() ?: initialProfile.heightCm,
                                weightKg = weight.replace(',', '.').toDoubleOrNull() ?: initialProfile.weightKg,
                                activityLevel = activityLevel,
                                goal = goal,
                                intolerances = intolerances.toList(),
                                allergies = allergies.toList(),
                                conditions = conditions.toList(),
                                completed = true
                            )
                            onComplete(profile)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(options: List<Pair<String, String>>, selected: MutableList<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (key, label) ->
            NbChip(
                text = label,
                selected = selected.contains(key),
                onClick = {
                    if (selected.contains(key)) selected.remove(key) else selected.add(key)
                }
            )
        }
    }
}

@Composable
private fun OnboardField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(4.dp),
        colors = TextFieldDefaults.colors(
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
    )
}
