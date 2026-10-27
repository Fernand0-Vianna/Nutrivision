package com.example.nutrivision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.nutrivision.ui.theme.NutrivisionTheme
import com.example.nutrivision.ui.screens.NutrivisionApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutrivisionTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NutrivisionApp()
                }
            }
        }
    }
}
