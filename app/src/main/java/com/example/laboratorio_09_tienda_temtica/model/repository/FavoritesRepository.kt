package com.example.laboratorio_09_tienda_temtica.model.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteBookIds(): Flow<List<String>>
    suspend fun addFavorite(bookId: String): Result<Unit>
    suspend fun removeFavorite(bookId: String): Result<Unit>
}
