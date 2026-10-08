package com.example.laboratorio_09_tienda_temtica.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT bookId FROM favorites ORDER BY bookId")
    fun observeFavoriteBookIds(): Flow<List<String>>
    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE bookId = :bookId")
    suspend fun deleteFavorite(bookId: String)
    @Query("SELECT * FROM order_lines ORDER BY bookId")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>
    @Upsert
    suspend fun upsertOrderLine(orderLine: OrderLineEntity)
    @Query("DELETE FROM order_lines WHERE bookId = :bookId")
    suspend fun deleteOrderLine(bookId: String)
    @Query("DELETE FROM order_lines")
    suspend fun clearOrder()
}