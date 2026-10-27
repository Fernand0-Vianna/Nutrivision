package com.example.nutrivision.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.data.UserProfile

@Composable
fun NutrivisionApp() {
    val context = LocalContext.current
    val repository = remember { RecordRepository(context) }
    var profile by remember { mutableStateOf(repository.loadUserProfile()) }
    var screen by rememberSaveable {
        mutableStateOf(if (profile.completed && profile.heightCm > 0.0) Screen.Home else Screen.Onboarding)
    }
    // Resultado do último scan, compartilhado entre Scan → Resultado
    var lastRecord by remember { mutableStateOf<FoodRecord?>(null) }

    BackHandler(enabled = screen != Screen.Home && screen != Screen.Onboarding && screen != Screen.Login) {
        screen = if (screen == Screen.Result) Screen.Scan else Screen.Home
    }

    BackHandler(enabled = screen == Screen.Onboarding && profile.completed) {
        screen = Screen.Profile
    }

    when (screen) {
        Screen.Onboarding -> OnboardingScreen(
            initialProfile = profile,
            onComplete = {
                val wasCompleted = profile.completed
                profile = it
                repository.saveUserProfile(it)
                if (it.weightKg > 0.0) {
                    runCatching { repository.saveWeight(it.weightKg) }
                }
                screen = if (wasCompleted) Screen.Profile else Screen.Home
            }
        )
        Screen.Login -> LoginScreen(onContinue = { screen = Screen.Home })

        Screen.Home -> HomeScreen(
            repository = repository,
            onScan = { screen = Screen.Scan },
            onNavigate = { screen = it }
        )

        Screen.Scan -> ScanScreen(
            repository = repository,
            onBack = { screen = Screen.Home },
            onAnalyzed = { record ->
                lastRecord = record
                screen = Screen.Result
            }
        )

        Screen.Result -> ResultScreen(
            repository = repository,
            record = lastRecord ?: FoodRecord(mealName = "Refeição"),
            onBack = { screen = Screen.Scan },
            onSaved = { screen = Screen.Analysis }
        )

        Screen.Analysis -> AnalysisScreen(
            repository = repository,
            onBack = { screen = Screen.Home }
        )

        Screen.Diary -> DiaryScreen(repository = repository, onNavigate = { screen = it })
        Screen.Progress -> ProgressScreen(repository = repository, onNavigate = { screen = it })
        Screen.Profile -> ProfileScreen(repository = repository, onNavigate = { screen = it })
    }
}

enum class Screen {
    Onboarding, Login, Home, Scan, Result, Analysis, Diary, Progress, Profile
}
