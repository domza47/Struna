package com.example.struna

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login successful!")
                } else {
                    onResult(false, task.exception?.message ?: "Login failed.")
                }
            }
    }

    fun registerUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Registration successful!")
                } else {
                    onResult(false, task.exception?.message ?: "Registration failed.")
                }
            }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}
