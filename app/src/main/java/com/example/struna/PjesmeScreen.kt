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

data class Pjesma(
    val id: String,
    val naziv: String,
    val izvodac: String,
    val akordi: String
)

val samplePjesme = listOf(
    Pjesma(
        id = "gdje_si_sad",
        naziv = "Gdje si sad",
        izvodac = "Parni Valjak",
        akordi = """
[G]Gdje si sada, [C]moja ljubavi
[D]Tko te ljubi dok sam [G]sam
[G]Tko ti pjeva [C]pjesme ljubavi
[D]Kad me nema da sam [G]tamo
""".trimIndent()
    ),
    Pjesma(
        id = "moja_prva_ljubav",
        naziv = "Moja Prva Ljubav",
        izvodac = "Hrvoje Hegedušić",
        akordi = """
[G]Moja prva [C]ljubav bio si [D]ti
[G]Ti si bio [C]moj prvi [D]san
[Em]S tobom sam [C]htjela
[G]Ostati [D]zauvijek
""".trimIndent()
    )
)


@Composable
fun PjesmeScreen() {
    val user = Firebase.auth.currentUser
    var selectedSongId by remember { mutableStateOf<String?>(null) }
    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Ucitaj favorite iz Firestore-a
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

    if (selectedSongId == null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Pjesme s akordima", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            samplePjesme.forEach { pjesma ->
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
                                    } else {
                                        favRef.delete()
                                    }
                                    favorites =
                                        if (!isFav) favorites + pjesma.id else favorites - pjesma.id
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
        val pjesma = samplePjesme.find { it.id == selectedSongId }
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
        }
    }
}

@Composable
fun AkordiLine(line: String) {
    val regex = "\\[([^]]+)]".toRegex()
    var lastIndex = 0
    Row {
        regex.findAll(line).forEach { match ->
            if (match.range.first > lastIndex) {
                Text(line.substring(lastIndex, match.range.first))
            }
            Text(
                match.value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
            lastIndex = match.range.last + 1
        }
        if (lastIndex < line.length) {
            Text(line.substring(lastIndex))
        }
    }
}
