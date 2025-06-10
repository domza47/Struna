package com.example.struna

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.clickable
import android.util.Log
import androidx.compose.material3.HorizontalDivider

@Composable
fun ScoreboardScreen() {
    val user = Firebase.auth.currentUser
    val availableTracks = listOf("Blues u A", "Dorian D")
    var selectedTrack by remember { mutableStateOf(availableTracks.first()) }
    var scores by remember { mutableStateOf<List<ScoreboardEntry>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    // Dohvati sve scoreove za odabranu skalu/track
    LaunchedEffect(selectedTrack) {
        Firebase.firestore.collection("scores")
            .whereEqualTo("track", selectedTrack)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { res ->
                scores = res.documents.mapNotNull { doc ->
                    val username = doc.getString("username") ?: "Nepoznato"
                    val score = doc.getLong("score") ?: return@mapNotNull null
                    val track = doc.getString("track") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    ScoreboardEntry(score, track, timestamp, username)
                }
            }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Scoreboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Dropdown za odabir skale (tracka)
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedTrack,
                onValueChange = {},
                label = { Text("Odaberi skalu") },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null,
                        Modifier.clickable { expanded = true })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                availableTracks.forEach { track ->
                    DropdownMenuItem(
                        text = { Text(track) },
                        onClick = {
                            selectedTrack = track
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (scores.isEmpty()) {
            Text("Još nema rezultata za odabranu skalu.")
        } else {
            scores.forEachIndexed { i, jam ->
                ScoreboardItem(
                    jam = jam,
                    place = i + 1,
                    isUser = jam.username == user?.displayName
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

data class ScoreboardEntry(
    val score: Long,
    val track: String,
    val timestamp: Long,
    val username: String
)

@Composable
fun ScoreboardItem(jam: ScoreboardEntry, place: Int, isUser: Boolean) {

    val dateStr = remember(jam.timestamp) {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy. HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(jam.timestamp))
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("$place. ${jam.username}", style = MaterialTheme.typography.bodyLarge)
            Text("Bodovi: ${jam.score}", style = MaterialTheme.typography.bodyMedium)
            Text("Vrijeme: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Track: ${jam.track}", style = MaterialTheme.typography.bodySmall)
        }
        if (isUser) {
            Text("tvoj rekord", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
