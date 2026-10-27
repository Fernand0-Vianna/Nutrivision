package com.example.nutrivision.data

/**
 * Peso corporal registrado pelo usuário. Apenas um valor por dia é mantido
 * (um novo registro no mesmo dia substitui o anterior).
 */
data class WeightEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val kg: Double = 0.0
)
