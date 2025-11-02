package com.uvg.mypokedex.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signInAnonymously(): String? {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}