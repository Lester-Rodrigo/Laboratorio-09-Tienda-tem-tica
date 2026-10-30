package com.example.laboratorio_09_tienda_temtica.data.preferences

import com.example.laboratorio_09_tienda_temtica.data.persistenceResult
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogPreferencesRepository
import kotlinx.coroutines.flow.Flow

class DataStoreCatalogPreferencesRepository(
    private val preferences: StorePreferences
) : CatalogPreferencesRepository {
    override fun observeSortOrder(): Flow<CatalogSortOrder> = preferences.sortOrder

    override suspend fun saveSortOrder(sortOrder: CatalogSortOrder): Result<Unit> =
        persistenceResult { preferences.saveSortOrder(sortOrder) }
}
