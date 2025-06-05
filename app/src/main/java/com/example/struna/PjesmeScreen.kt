package com.example.struna

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

data class Pjesma(
    val id: String = "",
    val naziv: String = "",
    val izvodac: String = "",
    val akordi: String = ""
)

@Composable
fun PjesmeScreen() {
    val user = Firebase.auth.currentUser
    var pjesme by remember { mutableStateOf<List<Pjesma>>(emptyList()) }
    var selectedSongId by remember { mutableStateOf<String?>(null) }
    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }

    // Učitaj pjesme iz Firestore
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("songs")
            .get()
            .addOnSuccessListener { res ->
                pjesme = res.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val naziv = doc.getString("naziv") ?: ""
                    val izvodac = doc.getString("izvodac") ?: ""
                    val akordi = doc.getString("akordi") ?: ""
                    if (naziv.isNotBlank() && izvodac.isNotBlank() && akordi.isNotBlank())
                        Pjesma(id, naziv, izvodac, akordi)
                    else null
                }
                loading = false
            }
    }
    // Učitaj favorite iz Firestore-a
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

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (selectedSongId == null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Pjesme s akordima", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            pjesme.forEach { pjesma ->
                val isFav = favorites.contains(pjesma.id)
                Card(
                    onClick = { selectedSongId = pjesma.id },
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
                        Column {
                            Text(pjesma.naziv, style = MaterialTheme.typography.titleMedium)
                            Text(pjesma.izvodac, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(
                            onClick = {
                                if (user != null) {
                                    val favRef = Firebase.firestore.collection("users")
                                        .document(user.uid)
                                        .collection("favorites")
                                        .document(pjesma.id)
                                    if (!isFav) {
                                        favRef.set(mapOf("id" to pjesma.id))
                                        favorites = favorites + pjesma.id
                                    } else {
                                        favRef.delete()
                                        favorites = favorites - pjesma.id
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFav) "Ukloni iz omiljenih" else "Dodaj u omiljene",
                                tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        val pjesma = pjesme.find { it.id == selectedSongId }
        if (pjesma != null) {
            PrikazPjesmeScreen(
                pjesma = pjesma,
                onBack = { selectedSongId = null }
            )
        }
    }
}

@Composable
fun PrikazPjesmeScreen(pjesma: Pjesma, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Button(onClick = onBack) {
            Text("Natrag")
        }
        Spacer(Modifier.height(16.dp))
        Text(pjesma.naziv, style = MaterialTheme.typography.headlineSmall)
        Text(pjesma.izvodac, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        pjesma.akordi.lines().forEach { linija ->
            AkordiLine(linija)
            Spacer(Modifier.height(2.dp))
        }
    }
}


@Composable
fun AkordiLine(line: String) {
    val regex = "\\[([^]]+)]".toRegex()
    val annotated = buildAnnotatedString {
        var lastIndex = 0
        regex.findAll(line).forEach { match ->
            // Dodaj tekst do akorda
            if (match.range.first > lastIndex) {
                append(line.substring(lastIndex, match.range.first))
            }
            // Dodaj akord s bojom
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        // Dodaj ostatak linije
        if (lastIndex < line.length) {
            append(line.substring(lastIndex))
        }
    }
    Text(annotated)
}
