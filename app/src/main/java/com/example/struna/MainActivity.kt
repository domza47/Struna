package com.example.struna

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            StrunaApp()
        }
    }
}

@Composable
fun StrunaApp() {
    var currentScreen by remember { mutableStateOf("login") }
    val context = LocalContext.current

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginClick = { email, password ->
                AuthManager.loginUser(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
                        currentScreen = "home"
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onNavigateToSignUp = {
                currentScreen = "signup"
            }
        )

        "signup" -> SignUpScreen(
            onSignUpClick = { email, password ->
                AuthManager.registerUser(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                        currentScreen = "home"
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onNavigateBackToLogin = {
                currentScreen = "login"
            }
        )

        "home" -> HomeScreen(
            onLogout = {
                AuthManager.logoutUser()
                currentScreen = "login"
            }
        )
    }
}

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎸 Welcome to Struna!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onLogout() }) {
            Text("Logout")
        }
    }
}
