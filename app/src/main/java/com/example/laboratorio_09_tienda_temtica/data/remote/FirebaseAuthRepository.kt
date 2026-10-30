package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {
    private val sessionMutex = Mutex()

    override suspend fun ensureAnonymousSession(): Result<Unit> = sessionMutex.withLock {
        if (auth.currentUser != null) {
            return@withLock Result.success(Unit)
        }

        try {
            auth.signInAnonymously().await()
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
