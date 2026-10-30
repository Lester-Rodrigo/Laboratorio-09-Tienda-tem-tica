package com.example.laboratorio_09_tienda_temtica.data.local

import com.example.laboratorio_09_tienda_temtica.data.persistenceResult
import com.example.laboratorio_09_tienda_temtica.model.CartItem
import com.example.laboratorio_09_tienda_temtica.model.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCartRepository(private val dao: StoreDao) : CartRepository {
    override fun observeItems(): Flow<List<CartItem>> =
        dao.observeOrderLines().map { entities -> entities.map { it.toCartItem() } }

    override suspend fun saveItem(item: CartItem): Result<Unit> = persistenceResult {
        dao.upsertOrderLine(item.toEntity())
    }

    override suspend fun removeItem(bookId: String): Result<Unit> = persistenceResult {
        dao.deleteOrderLine(bookId)
    }

    override suspend fun clearCart(): Result<Unit> = persistenceResult {
        dao.clearOrder()
    }
}
