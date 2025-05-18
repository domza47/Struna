package com.example.struna

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun JamModeScreen(
    viewModel: JamModeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onShowScoreboard: () -> Unit = {}
) {
    val backingTracks = listOf(
        BackingTrack("Blues u A", "a_blues", listOf("A", "C", "D", "E", "G")),
        BackingTrack("Dorian D", "d_dorian", listOf("D", "E", "F", "G", "A", "B", "C"))
    )

    var selectedTrack by remember { mutableStateOf(backingTracks.first()) }
    var isJamming by remember { mutableStateOf(false) }
    val score by viewModel.score.collectAsState()
    val lastNote by viewModel.lastNote.collectAsState()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    // Za prikaz rezultata nakon jam sessiona
    var showResults by remember { mutableStateOf(false) }
    var highScoreAchieved by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Jam Mode", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        DropdownMenuTrackPicker(
            tracks = backingTracks,
            selected = selectedTrack,
            onSelect = { selectedTrack = it }
        )
        Spacer(Modifier.height(8.dp))

        Text(
            "Dozvoljene note: ${selectedTrack.allowedNotes.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (isJamming) {
                    viewModel.stopJam()
                    isJamming = false
                } else {
                    viewModel.startJam(selectedTrack, ctx)
                    isJamming = true
                    showResults = false // resetiraj prikaz rezultata
                }
            }
        ) {
            Text(if (isJamming) "Zaustavi jam" else "Pokreni jam")
        }

        Spacer(Modifier.height(32.dp))

        if (isJamming) {
            Text("Bodovi: $score", style = MaterialTheme.typography.headlineSmall)
            Text("Sviraš: $lastNote", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    viewModel.saveScoreToFirebase { isHighScore ->
                        highScoreAchieved = isHighScore
                        showResults = true
                    }
                    viewModel.stopJam()
                    isJamming = false
                }
            }) {
                Text("Završi i spremi rezultat")
            }
        }

        if (showResults) {
            Spacer(Modifier.height(16.dp))
            if (highScoreAchieved) {
                Text(
                    "Novi rekord!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

@Composable
fun DropdownMenuTrackPicker(
    tracks: List<BackingTrack>,
    selected: BackingTrack,
    onSelect: (BackingTrack) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            label = { Text("Backing track") },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { expanded = true })
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            tracks.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
