package com.example.struna

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StimerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { /* Future: Instrument selection */ }) {
            Text("Gitara (EADGBE)")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.Green, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("G", style = MaterialTheme.typography.displayMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Prenisko",
            color = Color.Red,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
