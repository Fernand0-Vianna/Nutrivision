package com.example.nutrivision

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.RecordRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Testes de persistência com um diretório isolado (não tocam nos dados reais
 * do aparelho/emulador).
 */
@RunWith(AndroidJUnit4::class)
class RecordRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        val base = File(
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "repo_test_${System.nanoTime()}"
        ).apply { mkdirs() }
        context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getFilesDir(): File = File(base, "files").apply { mkdirs() }
            override fun getCacheDir(): File = File(base, "cache").apply { mkdirs() }
        }
    }

    @Test
    fun recordIsSavedLoadedAndDeleted() {
        val repository = RecordRepository(context)
        val record = FoodRecord(
            mealName = "Almoço",
            calories = 520.0,
            protein = 30.0,
            carbs = 60.0,
            fat = 18.0,
            source = "manual"
        )

        repository.saveRecord(record)
        val loaded = repository.loadRecords()
        assertEquals(1, loaded.size)
        assertEquals("Almoço", loaded.first().mealName)
        assertEquals(1, repository.recordsOfToday().size)
        assertEquals(1, repository.daysWithRecords(loaded))

        repository.deleteRecord(record.id)
        assertTrue(repository.loadRecords().isEmpty())
    }

    @Test
    fun weightUpdatesProfile() {
        val repository = RecordRepository(context)
        repository.saveUserProfile(
            com.example.nutrivision.data.UserProfile(
                name = "Teste",
                heightCm = 180.0,
                weightKg = 80.0,
                completed = true
            )
        )

        repository.saveWeight(76.4)

        assertEquals(1, repository.loadWeights().size)
        assertEquals(76.4, repository.loadUserProfile().weightKg, 0.001)
        assertEquals(76.4, repository.currentWeight()?.kg ?: 0.0, 0.001)
    }

    @Test
    fun waterIsAccumulatedAndResetPerDay() {
        val repository = RecordRepository(context)
        assertEquals(0.0, repository.loadWaterMl(), 0.001)

        repository.addWaterMl(250.0)
        repository.addWaterMl(500.0)
        assertEquals(750.0, repository.loadWaterMl(), 0.001)

        repository.resetWater()
        assertEquals(0.0, repository.loadWaterMl(), 0.001)
    }

    @Test
    fun exportCsvContainsAllSections() {
        val repository = RecordRepository(context)
        repository.saveRecord(
            FoodRecord(
                mealName = "Jantar",
                calories = 430.0,
                protein = 25.0,
                carbs = 40.0,
                fat = 14.0,
                source = "manual"
            )
        )
        repository.addWaterMl(250.0)
        repository.saveWeight(70.2)

        val file = repository.exportCsv()

        assertTrue(file.exists())
        val text = file.readText()
        assertTrue(text.contains("NUTRIVISION - RELATORIO"))
        assertTrue(text.contains("REGISTROS"))
        assertTrue(text.contains("AGUA POR DIA"))
        assertTrue(text.contains("EVOLUCAO DE PESO"))
        assertTrue(text.contains("Jantar"))
    }
}
