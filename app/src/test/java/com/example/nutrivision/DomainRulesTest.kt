package com.example.nutrivision

import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainRulesTest {

    @Test
    fun kcalPerRecordReturnsCalories() {
        val record = FoodRecord(calories = 400.0, protein = 20.0, carbs = 40.0, fat = 20.0)
        assertEquals(400.0, record.kcalPerRecord(), 0.001)
    }

    @Test
    fun bmiUsesHeightInMeters() {
        val profile = UserProfile(heightCm = 180.0, weightKg = 81.0)
        assertEquals(25.0, profile.bmi(), 0.01)
        assertEquals("Sobrepeso", profile.bmiCategory())
    }

    @Test
    fun allergiesAndConditionsProduceHealthAlerts() {
        val profile = UserProfile(
            allergies = listOf("AMENDOIM"),
            conditions = listOf("DIABETES")
        )
        val alerts = profile.healthAlerts()
        assertTrue(alerts.any { it.contains("Alergia") })
        assertTrue(alerts.any { it.contains("Diabetes") })
    }
}
