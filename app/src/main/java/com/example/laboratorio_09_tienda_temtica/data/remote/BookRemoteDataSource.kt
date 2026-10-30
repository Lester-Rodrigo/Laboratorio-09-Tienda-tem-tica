package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogReadError
import kotlinx.coroutines.flow.Flow

/** Raw document as stored in the `books` collection, without depending on Firebase types. */
data class RemoteBookDocument(
    val id: String,
    val fields: Map<String, Any?>?
)

data class RemoteBookSnapshot(
    val documents: List<RemoteBookDocument>,
    val isFromCache: Boolean = false
)

interface BookRemoteDataSource {
    /** Emits every catalog change; fails with [CatalogReadException] when the read is rejected. */
    fun observeBookDocuments(): Flow<RemoteBookSnapshot>
}

class CatalogReadException(
    val reason: CatalogReadError,
    cause: Throwable? = null
) : Exception("Catalog read failed: $reason", cause)
