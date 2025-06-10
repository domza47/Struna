package com.example.struna

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.struna.ui.theme.StrunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetronomScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun metronomScreen_displaysInitialState_andTogglesStartStop() {
        composeTestRule.setContent {
            StrunaTheme {
                MetronomScreen()
            }
        }

        // Provjeri prikaz BPM broja (default je 60)
        composeTestRule.onNodeWithText("60").assertExists()

        // Pronađi gumb START i klikni ga
        composeTestRule.onNodeWithText("START").assertExists().performClick()

        // Sad bi trebao biti prikazan STOP
        composeTestRule.onNodeWithText("STOP").assertExists()
    }
}
