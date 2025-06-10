package com.example.struna

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import androidx.compose.ui.platform.LocalContext


@Composable
fun PasswordResetScreen(
    onPasswordReset: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resetiraj lozinku", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (email.isNotBlank()) {
                Firebase.auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        message = if (task.isSuccessful)
                            "Poslan je email za reset lozinke."
                        else
                            "Greška: ${task.exception?.localizedMessage}"
                        if (task.isSuccessful) onPasswordReset()
                    }
            } else {
                message = "Upiši email."
            }
        }) {
            Text("Pošalji link za reset")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text("Natrag")
        }
        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
