package com.example.struna

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

@Composable
fun OmiljenePjesmeScreen() {
    val user = Firebase.auth.currentUser
    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedSongId by remember { mutableStateOf<String?>(null) }

    // Učitaj favorite ids iz Firestore-a
    LaunchedEffect(user?.uid) {
        if (user != null) {
            Firebase.firestore.collection("users").document(user.uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener { res ->
                    favorites = res.documents.mapNotNull { it.id }.toSet()
                }
        }
    }

    val omiljenePjesme = samplePjesme.filter { favorites.contains(it.id) }

    if (selectedSongId == null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Omiljene pjesme", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            if (omiljenePjesme.isEmpty()) {
                Text("Nema omiljenih pjesama.")
            } else {
                omiljenePjesme.forEach { pjesma ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clickable { selectedSongId = pjesma.id }
                            ) {
                                Text(pjesma.naziv, style = MaterialTheme.typography.titleMedium)
                                Text(pjesma.izvodac, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(
                                onClick = {
                                    if (user != null) {
                                        // Briši iz Firestore
                                        Firebase.firestore.collection("users").document(user.uid)
                                            .collection("favorites").document(pjesma.id)
                                            .delete()
                                        // Makni iz lokalne liste
                                        favorites = favorites - pjesma.id
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Ukloni iz omiljenih",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        val pjesma = samplePjesme.find { it.id == selectedSongId }
        if (pjesma != null) {
            PrikazPjesmeScreen(
                pjesma = pjesma,
                onBack = { selectedSongId = null }
            )
        }
    }
}
