package com.example.laboratorio_09_tienda_temtica.model.repository

import com.example.laboratorio_09_tienda_temtica.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun observeItems(): Flow<List<CartItem>>
    /** Saves an already validated quantity, replacing the existing line if present. */
    suspend fun saveItem(item: CartItem): Result<Unit>
    suspend fun removeItem(bookId: String): Result<Unit>
    suspend fun clearCart(): Result<Unit>
}
