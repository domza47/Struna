package com.example.struna

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
@Composable
fun OmiljenePjesmeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Omiljene pjesme - Your Favorites ❤️", style = MaterialTheme.typography.headlineMedium)
    }
}
