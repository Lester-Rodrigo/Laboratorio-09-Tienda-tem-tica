package com.example.laboratorio_09_tienda_temtica.model.repository

import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import kotlinx.coroutines.flow.Flow

interface CatalogPreferencesRepository {
    fun observeSortOrder(): Flow<CatalogSortOrder>
    suspend fun saveSortOrder(sortOrder: CatalogSortOrder): Result<Unit>
}
