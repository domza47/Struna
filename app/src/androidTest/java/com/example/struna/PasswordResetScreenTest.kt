package com.example.struna

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.struna.ui.theme.StrunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordResetScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userCanEnterEmailAndClickResetButton() {
        composeTestRule.setContent {
            StrunaTheme {
                PasswordResetScreen(
                    onPasswordReset = {}, // ignoriramo za sada
                    onBack = {}
                )
            }
        }

        // Unesi testni email
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")

        // Klikni gumb za reset
        composeTestRule.onNodeWithText("Pošalji link za reset").performClick()

        // Provjera da UI dopušta interakciju (bez Firebase)
        composeTestRule.onNodeWithText("Email").assertTextContains("test@example.com")
    }
}
