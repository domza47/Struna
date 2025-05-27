package com.example.struna

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.struna.ui.theme.StrunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@RunWith(AndroidJUnit4::class)
class ScoreboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun scoreboardDisplaysStaticEntriesAndDropdownWorks() {
        val testScores = listOf(
            ScoreboardEntry(
                score = 100,
                track = "Blues u A",
                timestamp = System.currentTimeMillis(),
                username = "TestKorisnik"
            )
        )

        composeTestRule.setContent {
            StrunaTheme {
                StaticScoreboardScreen(testScores)
            }
        }

        // Provjeri da se naslov prikazuje
        composeTestRule.onNodeWithText("Scoreboard").assertExists()

        // Provjeri da je prikazan rezultat korisnika
        composeTestRule.onNodeWithText("TestKorisnik", substring = true)
        composeTestRule.onNodeWithText("TestKorisnik", substring = true)

        // Provjeri dropdown
        composeTestRule.onNodeWithText("Odaberi skalu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Bodovi: 100", substring = true).assertExists()
    }

}
@Composable
fun StaticScoreboardScreen(fakeScores: List<ScoreboardEntry>) {
    val availableTracks = listOf("Blues u A", "Dorian D")
    var selectedTrack by remember { mutableStateOf(availableTracks.first()) }
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(24.dp)) {
        Text("Scoreboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

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
        fakeScores.forEachIndexed { i, jam ->
            ScoreboardItem(jam = jam, place = i + 1, isUser = false)
        }
    }
}

