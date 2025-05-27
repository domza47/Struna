package com.example.struna

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.tasks.Task
import io.mockk.*
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class AuthManagerTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var task: Task<AuthResult>
    private lateinit var user: FirebaseUser

    @Before
    fun setup() {
        auth = mockk()
        task = mockk(relaxed = true)
        user = mockk()
        // mock za AuthManager
        AuthManager.auth = auth

        every { auth.currentUser } returns user
        every { auth.signOut() } just Runs
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `loginUser - Successful login with valid credentials`() {
        every { auth.signInWithEmailAndPassword("test@test.com", "lozinka") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns true
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = false
        var message = ""

        AuthManager.loginUser("test@test.com", "lozinka") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(true, result)
        assertEquals("Prijava uspješna!", message)
    }

    @Test
    fun `loginUser - Failed login with incorrect password`() {
        val errorMessage = "The password is invalid or the user does not have a password."
        every { auth.signInWithEmailAndPassword("test@test.com", "kriva") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception(errorMessage)
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = true
        var message = ""

        AuthManager.loginUser("test@test.com", "kriva") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(false, result)
        assertEquals(errorMessage, message)
    }

    @Test
    fun `loginUser - Failed login with non-existent email`() {
        val errorMessage = "There is no user record corresponding to this identifier."
        every { auth.signInWithEmailAndPassword("nema@user.com", "lozinka") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception(errorMessage)
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = true
        var message = ""

        AuthManager.loginUser("nema@user.com", "lozinka") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(false, result)
        assertEquals(errorMessage, message)
    }

    @Test
    fun `loginUser - Failed login with invalid email format`() {
        val errorMessage = "The email address is badly formatted."
        every { auth.signInWithEmailAndPassword("neispravanemail", "lozinka") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception(errorMessage)
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = true
        var message = ""

        AuthManager.loginUser("neispravanemail", "lozinka") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(false, result)
        assertEquals(errorMessage, message)
    }

    @Test
    fun `loginUser - Failed login with empty email`() {
        val errorMessage = "The email address is empty."
        every { auth.signInWithEmailAndPassword("", "lozinka") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception(errorMessage)
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = true
        var message = ""

        AuthManager.loginUser("", "lozinka") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(false, result)
        assertEquals(errorMessage, message)
    }

    @Test
    fun `loginUser - Failed login with empty password`() {
        val errorMessage = "The password is empty."
        every { auth.signInWithEmailAndPassword("test@test.com", "") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception(errorMessage)
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = true
        var message = ""

        AuthManager.loginUser("test@test.com", "") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(false, result)
        assertEquals(errorMessage, message)
    }

    @Test
    fun `loginUser - Firebase authentication service unavailable`() {
        val errorMessage = "Network error, service unavailable"
        every { auth.signInWithEmailAndPassword("test@test.com", "lozinka") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception(errorMessage)
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = true
        var message = ""

        AuthManager.loginUser("test@test.com", "lozinka") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(false, result)
        assertEquals(errorMessage, message)
    }

    @Test
    fun `loginUser - Firebase task completes but exception is null`() {
        every { auth.signInWithEmailAndPassword("test@test.com", "lozinka") } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            every { task.isSuccessful } returns false
            every { task.exception } returns null
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }

        var result = true
        var message = ""

        AuthManager.loginUser("test@test.com", "lozinka") { ok, msg ->
            result = ok
            message = msg
        }

        assertEquals(false, result)
        assertEquals("Prijava nije uspjela.", message)
    }
}
