package com.example.struna

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun BodoviIzJamovaScreen() {
    val user = Firebase.auth.currentUser
    var jams by remember { mutableStateOf<List<UserJamScore>>(emptyList()) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            Firebase.firestore.collection("users").document(uid)
                .collection("jams")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { res ->
                    jams = res.documents.mapNotNull { doc ->
                        val score = doc.getLong("score") ?: return@mapNotNull null
                        val track = doc.getString("track") ?: "Nepoznata pjesma"
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        UserJamScore(score, track, timestamp)
                    }
                }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Tvoji prijašnji jamovi", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (jams.isEmpty()) {
            Text("Nema još spremljenih jamova.")
        } else {
            jams.forEach { jam ->
                JamScoreItem(jam)
                Divider()
            }
        }
    }
}

data class UserJamScore(val score: Long, val track: String, val timestamp: Long)

@Composable
fun JamScoreItem(jam: UserJamScore) {
    val dateStr = remember(jam.timestamp) {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy. HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(jam.timestamp))
    }
    Column(Modifier.padding(vertical = 8.dp)) {
        Text("Bodovi: ${jam.score}", style = MaterialTheme.typography.bodyLarge)
        Text("Pjesma: ${jam.track}", style = MaterialTheme.typography.bodyMedium)
        Text("Vrijeme: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
