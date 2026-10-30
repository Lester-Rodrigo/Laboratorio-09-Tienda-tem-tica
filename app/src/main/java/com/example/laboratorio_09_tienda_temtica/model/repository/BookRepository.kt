package com.example.laboratorio_09_tienda_temtica.model.repository

import com.example.laboratorio_09_tienda_temtica.model.Books
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun observeCatalog(): Flow<CatalogResult>
}

/** Distinguishes a read failure, defective documents and a catalog that is really empty. */
sealed interface CatalogResult {
    /** At least one valid book; [invalidDocumentIds] lists the documents that were skipped. */
    data class Available(
        val books: List<Books>,
        val invalidDocumentIds: List<String> = emptyList()
    ) : CatalogResult

    /** The collection was read correctly and has no documents. */
    data object Empty : CatalogResult

    /** The collection has documents, but none of them could be converted into a book. */
    data class InvalidDocuments(val invalidDocumentIds: List<String>) : CatalogResult

    data class ReadError(val reason: CatalogReadError) : CatalogResult
}

enum class CatalogReadError {
    CONNECTION,
    PERMISSION_DENIED,
    UNKNOWN
}
