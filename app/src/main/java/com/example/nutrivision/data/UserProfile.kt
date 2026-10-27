package com.example.nutrivision.data

import java.util.Locale
import kotlin.math.pow

data class UserProfile(
    val name: String = "",
    val age: Int? = null,
    val sex: String = "", // "M" | "F" | "OUTRO"
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val activityLevel: String = "MODERADO", // SEDENTARIO | LEVE | MODERADO | ATIVO | MUITO_ATIVO
    val goal: String = "MANTER", // PERDER | MANTER | GANHAR
    val intolerances: List<String> = emptyList(), // LACTOSE, GLUTEN, FRUTOS_DO_MAR, OVO, SOJA, AMENDOIM, NOZES, CASTANHAS
    val allergies: List<String> = emptyList(),
    val conditions: List<String> = emptyList(), // DIABETES, HIPERTENSAO, COLESTEROL_ALTO, DOENCA_RENAL, OUTRA
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun bmi(): Double {
        if (heightCm <= 0.0 || weightKg <= 0.0) return 0.0
        val heightM = heightCm / 100.0
        return weightKg / heightM.pow(2.0)
    }

    fun bmiCategory(): String {
        val value = bmi()
        return when {
            value <= 0.0 -> "Não calculado"
            value < 18.5 -> "Abaixo do peso"
            value < 25.0 -> "Peso normal"
            value < 30.0 -> "Sobrepeso"
            value < 35.0 -> "Obesidade grau I"
            value < 40.0 -> "Obesidade grau II"
            else -> "Obesidade grau III"
        }
    }

    fun bmiColorHint(): String {
        val value = bmi()
        return when {
            value <= 0.0 -> "MUTED"
            value < 18.5 -> "INFO"
            value < 25.0 -> "SUCCESS"
            value < 30.0 -> "WARNING"
            else -> "DANGER"
        }
    }

    fun bmiFormatted(): String {
        val value = bmi()
        return if (value <= 0.0) "--" else String.format(Locale("pt", "BR"), "%.1f", value)
    }

    fun hasRestriction(item: String): Boolean {
        val key = item.uppercase(Locale.ROOT)
        return intolerances.any { it.uppercase(Locale.ROOT) == key } ||
            allergies.any { it.uppercase(Locale.ROOT) == key }
    }

    /** Avisos personalizados para exibir no perfil e no resultado do scan. */
    fun healthAlerts(): List<String> {
        val out = mutableListOf<String>()
        allergies.forEach { key ->
            out.add("Alergia a ${ProfileOptions.label(key)} (risco alto): evite o alimento e confira rótulos e traços.")
        }
        intolerances.filterNot { key -> allergies.any { it.equals(key, ignoreCase = true) } }.forEach { key ->
            val detail = when (key) {
                "LACTOSE" -> "leite, queijo, manteiga e creme podem causar desconforto."
                "GLUTEN" -> "verifique trigo, cevada, centeio e traços em rótulos."
                "FRUTOS_DO_MAR" -> "atenção a caldos, molhos e risotos."
                "OVO" -> "pode aparecer em massas, empanados e molhos."
                "SOJA" -> "presente em óleos, molhos e produtos industrializados."
                "AMENDOIM" -> "risco de traços em doces, molhos e snacks."
                "NOZES" -> "risco de traços em granolas e produtos de padaria."
                "CASTANHAS" -> "risco de traços em granolas e produtos de padaria."
                else -> "confira os ingredientes antes de consumir."
            }
            out.add("Intolerância a ${ProfileOptions.label(key)}: $detail")
        }
        conditions.forEach { key ->
            val detail = when (key) {
                "DIABETES" -> "monitore carboidratos e açúcares; a estimativa por foto é aproximada."
                "HIPERTENSAO" -> "atenção ao sódio de molhos, embutidos e temperos prontos."
                "COLESTEROL_ALTO" -> "prefira gorduras insaturadas e limite frituras."
                "DOENCA_RENAL" -> "confirme metas de proteína, potássio e sódio com seu médico."
                else -> "siga a orientação do seu profissional de saúde."
            }
            out.add("${ProfileOptions.label(key)}: $detail")
        }
        return out
    }
}

/** Opções e rótulos legíveis usados no onboarding e no perfil. */
object ProfileOptions {

    val intolerances = listOf(
        "LACTOSE" to "Lactose",
        "GLUTEN" to "Glúten",
        "FRUTOS_DO_MAR" to "Frutos do mar",
        "OVO" to "Ovo",
        "SOJA" to "Soja",
        "AMENDOIM" to "Amendoim",
        "NOZES" to "Nozes",
        "CASTANHAS" to "Castanhas"
    )

    val allergies = listOf(
        "AMENDOIM" to "Amendoim",
        "NOZES" to "Nozes",
        "CASTANHAS" to "Castanhas",
        "FRUTOS_DO_MAR" to "Frutos do mar",
        "OVO" to "Ovo",
        "SOJA" to "Soja",
        "GLUTEN" to "Glúten",
        "LACTOSE" to "Lactose"
    )

    val conditions = listOf(
        "DIABETES" to "Diabetes",
        "HIPERTENSAO" to "Hipertensão",
        "COLESTEROL_ALTO" to "Colesterol alto",
        "DOENCA_RENAL" to "Doença renal",
        "OUTRA" to "Outra"
    )

    fun label(key: String): String =
        (intolerances + conditions).firstOrNull { it.first == key }?.second
            ?: key.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }

    fun sexLabel(key: String): String = when (key) {
        "M" -> "Masculino"
        "F" -> "Feminino"
        "OUTRO" -> "Outro"
        else -> "Não informado"
    }

    fun activityLabel(key: String): String = when (key) {
        "SEDENTARIO" -> "Sedentário"
        "LEVE" -> "Leve"
        "MODERADO" -> "Moderado"
        "ATIVO" -> "Ativo"
        "MUITO_ATIVO" -> "Muito ativo"
        else -> key
    }

    fun goalLabel(key: String): String = when (key) {
        "PERDER" -> "Perder peso"
        "MANTER" -> "Manter peso"
        "GANHAR" -> "Ganhar massa"
        else -> key
    }

    fun labels(keys: List<String>): String = keys.joinToString(", ") { label(it) }
}
