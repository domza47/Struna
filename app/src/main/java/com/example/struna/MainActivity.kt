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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

// OVAKO: Sada nema dodatnih import-a za screens (sve ostaje u istom paketu)

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
                }
            }
        }
    }
}

// ➡️ Ovo je funkcija koja dinamički vraća pravi naslov
fun getTitleForScreen(screen: String): String {
    return when (screen) {
        "stimer" -> "Štimer"
        "jam" -> "Jam Mode"
        "akordi" -> "Akordi"
        "metronom" -> "Metronom"
        "scoreboard" -> "Scoreboard"
        "pjesme" -> "Pjesme"
        "favorites" -> "Omiljene pjesme"
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
