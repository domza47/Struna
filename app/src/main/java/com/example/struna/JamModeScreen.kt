package com.example.struna

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
@Composable
fun JamModeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Jam Mode - Coming soon 🎸", style = MaterialTheme.typography.headlineMedium)
    }
}
