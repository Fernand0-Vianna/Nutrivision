package com.example.nutrivision

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.data.UserProfile
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navegação ponta a ponta sem depender de `adb input` (bloqueado pelo MIUI).
 * Os cliques são injetados pela instrumentação, dentro do processo do app.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    companion object {
        /** Evita o onboarding nos testes de navegação (ele é testado à parte). */
        @JvmStatic
        @BeforeClass
        fun seedProfile() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            RecordRepository(context).saveUserProfile(
                UserProfile(
                    name = "Teste Automatizado",
                    age = 30,
                    sex = "OUTRO",
                    heightCm = 175.0,
                    weightKg = 70.0,
                    completed = true
                )
            )
        }
    }

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginAndAllTabsAreReachable() {
        compose.onNodeWithText("META DO DIA").assertIsDisplayed()

        compose.onNodeWithText("DIÁRIO").performClick()
        compose.onNodeWithText("DIÁRIO ALIMENTAR").assertIsDisplayed()

        compose.onNodeWithText("PROGRESSO").performClick()
        compose.onNodeWithText("PROGRESSO E INSIGHTS").assertIsDisplayed()

        compose.onNodeWithText("PERFIL").performClick()
        compose.onNodeWithText("METAS DIÁRIAS").assertIsDisplayed()

        compose.onNodeWithText("INÍCIO").performClick()
        compose.onNodeWithText("META DO DIA").assertIsDisplayed()
    }

    @Test
    fun scanScreenOpensAndCloses() {
        compose.onNodeWithText("SCAN").performClick()
        compose.onNodeWithText("APONTE PARA O ALIMENTO").assertIsDisplayed()
        compose.onNodeWithText("TIRAR FOTO").assertIsDisplayed()
        compose.onNodeWithText("ESCOLHER DA GALERIA").assertIsDisplayed()
        compose.onNodeWithText("×").performClick()
        compose.onNodeWithText("META DO DIA").assertIsDisplayed()
    }

    @Test
    fun profileEditOpensOnboarding() {
        compose.onNodeWithText("PERFIL").performClick()
        compose.onNodeWithText("EDITAR PERFIL E RESTRIÇÕES").performClick()
        compose.onNodeWithText("ATUALIZAR PERFIL").assertIsDisplayed()
        compose.onNodeWithText("DADOS PESSOAIS").assertIsDisplayed()
        Espresso.pressBack()
        compose.onNodeWithText("METAS DIÁRIAS").assertIsDisplayed()
    }

    @Test
    fun diaryRangeAndMealChipsReact() {
        compose.onNodeWithText("DIÁRIO").performClick()

        compose.onNodeWithTag("range_HOJE").performClick()
        compose.onNodeWithTag("range_7 DIAS").performClick()
        compose.onNodeWithTag("range_30 DIAS").performClick()
        compose.onNodeWithTag("range_TUDO").performClick()

        compose.onNodeWithTag("meal_ALMOÇO").performClick()
        compose.onNodeWithTag("meal_TODOS").performClick()
        compose.onNodeWithText("DIÁRIO ALIMENTAR").assertIsDisplayed()
    }
}
