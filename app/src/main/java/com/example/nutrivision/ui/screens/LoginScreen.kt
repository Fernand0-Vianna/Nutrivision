package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.ui.components.NbButtonPrimary
import com.example.nutrivision.ui.theme.*

@Composable
fun LoginScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bone)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "NUTRIVISION",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "REGISTRE SUAS REFEIÇÕES\nE VEJA O QUE VOCÊ COME",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Secondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        NbButtonPrimary(
            text = "Começar",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
