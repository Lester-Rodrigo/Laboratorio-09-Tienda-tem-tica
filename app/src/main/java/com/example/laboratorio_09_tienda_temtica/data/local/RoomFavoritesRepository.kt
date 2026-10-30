package com.example.laboratorio_09_tienda_temtica.data.local

import com.example.laboratorio_09_tienda_temtica.data.persistenceResult
import com.example.laboratorio_09_tienda_temtica.model.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class RoomFavoritesRepository(private val dao: StoreDao) : FavoritesRepository {
    override fun observeFavoriteBookIds(): Flow<List<String>> = dao.observeFavoriteBookIds()

    override suspend fun addFavorite(bookId: String): Result<Unit> = persistenceResult {
        dao.upsertFavorite(FavoriteEntity(bookId))
    }

    override suspend fun removeFavorite(bookId: String): Result<Unit> = persistenceResult {
        dao.deleteFavorite(bookId)
    }
}
