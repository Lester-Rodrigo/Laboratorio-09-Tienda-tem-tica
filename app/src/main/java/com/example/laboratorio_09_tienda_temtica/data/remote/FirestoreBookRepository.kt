package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogReadError
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogResult
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FirestoreBookRepository(
    private val dataSource: BookRemoteDataSource
) : BookRepository {

    override fun observeCatalog(): Flow<CatalogResult> =
        dataSource.observeBookDocuments()
            .map { snapshot -> snapshot.toCatalogResult() }
            .catch { error -> emit(CatalogResult.ReadError(error.toCatalogReadError())) }
            .distinctUntilChanged()
}

internal fun RemoteBookSnapshot.toCatalogResult(): CatalogResult {
    if (documents.isEmpty()) {
        // Offline without cache Firestore answers with an empty local snapshot, not with an error.
        return if (isFromCache) {
            CatalogResult.ReadError(CatalogReadError.CONNECTION)
        } else {
            CatalogResult.Empty
        }
    }

    val books = mutableListOf<Books>()
    val invalidDocumentIds = mutableListOf<String>()
    documents.forEach { document ->
        val book = document.toBookOrNull()
        if (book != null) {
            books.add(book)
        } else {
            invalidDocumentIds.add(document.id)
        }
    }

    return if (books.isEmpty()) {
        CatalogResult.InvalidDocuments(invalidDocumentIds)
    } else {
        CatalogResult.Available(books, invalidDocumentIds)
    }
}

private fun Throwable.toCatalogReadError(): CatalogReadError = when (this) {
    is CatalogReadException -> reason
    is IOException -> CatalogReadError.CONNECTION
    else -> CatalogReadError.UNKNOWN
}
