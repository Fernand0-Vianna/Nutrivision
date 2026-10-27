package com.example.nutrivision.data

/**
 * Metas diárias do usuário. Persistidas junto aos registros.
 */
data class Goals(
    val calories: Double = 2000.0,
    val protein: Double = 120.0,
    val carbs: Double = 250.0,
    val fat: Double = 65.0,
    val waterMl: Double = 2500.0
)

/**
 * Agregados calculados a partir dos registros — usados nas telas de
 * Diário, Progresso e Perfil.
 */
data class DailyStats(
    val recordCount: Int = 0,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    /** Média aritmética (calorias, proteína, carbo, gordura) por registro. */
    val averagePerRecord: Double = 0.0,
    /** Média global das médias por registro do histórico. */
    val averageOfAverages: Double = 0.0,
    val bestRecord: FoodRecord? = null,
    val mealBreakdown: Map<String, Int> = emptyMap()
)
