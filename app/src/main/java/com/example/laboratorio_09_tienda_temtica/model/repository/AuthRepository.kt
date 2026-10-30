package com.example.laboratorio_09_tienda_temtica.model.repository

interface AuthRepository {
    suspend fun ensureAnonymousSession(): Result<Unit>
}
