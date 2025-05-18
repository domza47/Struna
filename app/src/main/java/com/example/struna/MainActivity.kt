package com.example.struna

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

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
                        Toast.makeText(context, "Dobrodošli!", Toast.LENGTH_SHORT).show()
                        currentScreen = "home"
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onNavigateToSignUp = { currentScreen = "signup" },
            onGoogleLoginSuccess = {
                // Novi flow za Google prijavu!
                checkUserHasUsername(
                    onHasUsername = { currentScreen = "home" },
                    onMissingUsername = { currentScreen = "addUsername" }
                )
            },
            onForgotPasswordClick = { currentScreen = "reset" }
        )

        "signup" -> SignUpScreen(
            onSignUpClick = { email, password, username ->
                AuthManager.register(
                    email, password, username,
                    onSuccess = {
                        Toast.makeText(context, "Račun kreiran!", Toast.LENGTH_SHORT).show()
                        currentScreen = "home"
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onNavigateBackToLogin = { currentScreen = "login" }
        )

        "reset" -> PasswordResetScreen(
            onPasswordReset = {
                Toast.makeText(context, "Provjeri email za upute.", Toast.LENGTH_SHORT).show()
                currentScreen = "login"
            },
            onBack = { currentScreen = "login" }
        )

        "addUsername" -> AddUsernameScreen(
            onUsernameSaved = { currentScreen = "home" }
        )

        "home" -> HomeScreen(
            onLogout = {
                AuthManager.logoutUser()
                currentScreen = "login"
            }
        )
    }
}



fun checkUserHasUsername(
    onHasUsername: () -> Unit,
    onMissingUsername: () -> Unit
) {
    val user = Firebase.auth.currentUser ?: return
    Firebase.firestore.collection("users").document(user.uid).get()
        .addOnSuccessListener { doc ->
            val username = doc.getString("username")
            if (username.isNullOrBlank()) {
                onMissingUsername()
            } else {
                onHasUsername()
            }
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var expandedProfileMenu by remember { mutableStateOf(false) }
    var selectedScreen by remember { mutableStateOf("stimer") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerItem("Štimer") { selectedScreen = "stimer"; scope.launch { drawerState.close() } }
                DrawerItem("Jam Mode") { selectedScreen = "jam"; scope.launch { drawerState.close() } }
                DrawerItem("Akordi") { selectedScreen = "akordi"; scope.launch { drawerState.close() } }
                DrawerItem("Metronom") { selectedScreen = "metronom"; scope.launch { drawerState.close() } }
                DrawerItem("Scoreboard") { selectedScreen = "scoreboard"; scope.launch { drawerState.close() } }
                DrawerItem("Pjesme") { selectedScreen = "pjesme"; scope.launch { drawerState.close() } }
                DrawerItem("Bodovi") { selectedScreen = "bodovi"; scope.launch { drawerState.close() } }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getTitleForScreen(selectedScreen)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { expandedProfileMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Profile")
                        }
                        DropdownMenu(
                            expanded = expandedProfileMenu,
                            onDismissRequest = { expandedProfileMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Omiljene pjesme") },
                                onClick = {
                                    expandedProfileMenu = false
                                    selectedScreen = "favorites"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bodovi iz jamova") },
                                onClick = {
                                    expandedProfileMenu = false
                                    selectedScreen = "bodovi"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Odjava") },
                                onClick = {
                                    expandedProfileMenu = false
                                    onLogout()
                                }
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedScreen) {
                    "stimer" -> StimerScreen()
                    "jam" -> JamModeScreen()
                    "akordi" -> AkordiScreen()
                    "metronom" -> MetronomScreen()
                    "scoreboard" -> ScoreboardScreen()
                    "pjesme" -> PjesmeScreen()
                    "favorites" -> OmiljenePjesmeScreen()
                    "bodovi" -> BodoviIzJamovaScreen()
                }
            }
        }
    }
}

// Ovo je funkcija koja dinamički vraća pravi naslov
fun getTitleForScreen(screen: String): String {
    return when (screen) {
        "stimer" -> "Štimer"
        "jam" -> "Jam Mode"
        "akordi" -> "Akordi"
        "metronom" -> "Metronom"
        "scoreboard" -> "Scoreboard"
        "pjesme" -> "Pjesme"
        "favorites" -> "Omiljene pjesme"
        "bodovi" -> "Bodovi iz jamova"
        else -> "Struna"
    }
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}


