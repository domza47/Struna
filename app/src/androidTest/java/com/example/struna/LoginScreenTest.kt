package com.example.struna

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.struna.ui.theme.StrunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreen_userCanEnterEmailAndPasswordAndClickLogin() {
        composeTestRule.setContent {
            StrunaTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        // Ovdje možeš postaviti testne zastavice ako trebaš nešto potvrditi
                        assert(email == "test@email.com")
                        assert(password == "lozinka123")
                    },
                    onNavigateToSignUp = {},
                    onGoogleLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Unesi email
        composeTestRule.onNodeWithText("Email").performTextInput("test@email.com")

        // Unesi lozinku
        composeTestRule.onNodeWithText("Lozinka").performTextInput("lozinka123")

        // Klikni "Prijavi se"
        composeTestRule.onNodeWithText("Prijavi se").performClick()
    }
}
