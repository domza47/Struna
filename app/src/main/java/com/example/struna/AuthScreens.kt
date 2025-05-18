package com.example.struna

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGoogleLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = Firebase.auth

    // Zamijeni s tvojim pravim WEB CLIENT ID-om!
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("441618247222-08h4b310d5vpscl58irqap9gnt7arjqj.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val userRef = Firebase.firestore.collection("users").document(user.uid)
                            userRef.get().addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    val userData = hashMapOf(
                                        "uid" to user.uid,
                                        "email" to user.email
                                    )
                                    userRef.set(userData)
                                }
                                Toast.makeText(context, "Prijava Google-om uspješna!", Toast.LENGTH_SHORT).show()
                                onGoogleLoginSuccess()
                            }
                        } else {
                            Toast.makeText(context, "Greška pri Google prijavi.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Greška pri Google prijavi.", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Struna: prijava", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Lozinka") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onLoginClick(email, password) }) {
            Text("Prijavi se")
        }

        TextButton(onClick = { onForgotPasswordClick() }) {
            Text("Zaboravljena lozinka?")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                launcher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Prijavi se s Google računom")
        }

        TextButton(onClick = onNavigateToSignUp) {
            Text("Nemaš profil? Registriraj se.")
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUpClick: (email: String, password: String, username: String) -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Struna: registracija", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Korisničko ime") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Lozinka") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSignUpClick(email, password, username) }) {
            Text("Registriraj se")
        }

        TextButton(onClick = onNavigateBackToLogin) {
            Text("Već imaš račun? Prijavi se.")
        }
    }
}

@Composable
fun AddUsernameScreen(
    onUsernameSaved: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Odaberi korisničko ime", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("Korisničko ime") },
            isError = error != null,
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (username.isBlank()) {
                error = "Unesi korisničko ime"
                return@Button
            }
            // Provjeri zauzetost
            Firebase.firestore.collection("users")
                .whereEqualTo("usernameLower", username.lowercase())
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        val user = Firebase.auth.currentUser
                        val data = mapOf(
                            "username" to username,
                            "usernameLower" to username.lowercase()
                        )
                        Firebase.firestore.collection("users").document(user!!.uid)
                            .update(data)
                            .addOnSuccessListener { onUsernameSaved() }
                            .addOnFailureListener { error = "Greška: ${it.localizedMessage}" }
                    } else {
                        error = "To korisničko ime je već zauzeto!"
                    }
                }
        }) {
            Text("Spremi korisničko ime")
        }
    }
}