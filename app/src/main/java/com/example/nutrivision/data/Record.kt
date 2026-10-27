package com.example.nutrivision.data

data class FoodRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val mealName: String = "",
    /** Categoria da refeição: "Café", "Almoço", "Jantar", "Lanche", "Outro". */
    val mealType: String = "",
    val imagePath: String? = null,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    /**
     * "gemini" quando a API analisou a foto; "manual" quando o usuário digitou
     * os valores; "fixado" quando veio de um prato salvo na tela inicial.
     */
    val source: String = "manual"
) {
    /** Calorias por registro — métrica nutricional válida. */
    fun kcalPerRecord(): Double = calories
}

/**
 * Prato salvo pelo usuário para ser adicionado ao diário com um toque.
 * É um retrato dos valores no momento em que foi fixado — não acompanha
 * alterações posteriores em registros antigos.
 */
data class PinnedMeal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Refeição",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val mealType: String = "",
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun matches(name: String, calories: Double): Boolean =
        this.name.trim().equals(name.trim(), ignoreCase = true) &&
            kotlin.math.abs(this.calories - calories) < 1.0

    fun toRecord(timestamp: Long = System.currentTimeMillis()): FoodRecord =
        FoodRecord(
            timestamp = timestamp,
            mealName = name.ifBlank { "Refeição" },
            mealType = mealType,
            imagePath = imagePath,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            source = "fixado"
        )
}
