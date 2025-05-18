package com.example.struna

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Prijava uspješna!")
                } else {
                    onResult(false, task.exception?.message ?: "Prijava nije uspjela.")
                }
            }
    }

    fun register(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usernameLower = username.trim().lowercase()
        val db = Firebase.firestore

        // 1. Provjeri postoji li username (case-insensitive)
        db.collection("users")
            .whereEqualTo("usernameLower", usernameLower)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    onError("Korisničko ime je zauzeto!")
                    return@addOnSuccessListener
                }

                // 2. Ako nije zauzeto, napravi korisnika
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            // Dodaj displayName u Auth profil
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()
                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                // Spremi i u Firestore "users"
                                val userData = hashMapOf(
                                    "uid" to user.uid,
                                    "username" to username,
                                    "usernameLower" to usernameLower,
                                    "email" to email
                                )
                                db.collection("users").document(user.uid).set(userData)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e -> onError("Greška u Firestore: ${e.localizedMessage}") }
                            }
                        } else {
                            onError(task.exception?.localizedMessage ?: "Neuspješna registracija")
                        }
                    }
            }
            .addOnFailureListener { e ->
                onError("Greška pri provjeri korisničkog imena: ${e.localizedMessage}")
            }
    }


    fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}
