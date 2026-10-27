package com.example.nutrivision.data

data class FoodRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val mealName: String = "",
    val imagePath: String? = null,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    /** "gemini" quando a API analisou a foto; "manual" quando o usuário digitou os valores. */
    val source: String = "manual"
) {
    fun average(): Double {
        val values = listOf(calories, protein, carbs, fat)
        val sum = values.sum()
        return if (values.isNotEmpty()) sum / values.size else 0.0
    }
}
