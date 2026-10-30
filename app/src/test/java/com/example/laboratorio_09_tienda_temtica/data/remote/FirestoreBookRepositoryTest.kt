package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogReadError
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogResult
import java.io.IOException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreBookRepositoryTest {

    private val dataSource = FakeBookRemoteDataSource()
    private val repository = FirestoreBookRepository(dataSource)

    @Test
    fun initialLoadEmitsAvailableCatalog() = runBlocking {
        dataSource.emit(bookDocument("book-1"), bookDocument("book-2"))

        val result = repository.observeCatalog().take(1).toList().single()

        result as CatalogResult.Available
        assertEquals(listOf("book-1", "book-2"), result.books.map { it.id })
        assertTrue(result.invalidDocumentIds.isEmpty())
    }

    @Test
    fun firebaseUpdateEmitsNewCatalog() = runBlocking {
        dataSource.emit(bookDocument("book-1"))
        dataSource.emit(
            bookDocument("book-1", validBookFields(price = 95.0, stock = 2L)),
            bookDocument("book-2")
        )

        val results = repository.observeCatalog().take(2).toList()

        val initial = results[0] as CatalogResult.Available
        val updated = results[1] as CatalogResult.Available
        assertEquals(listOf(8), initial.books.map { it.stock })
        assertEquals(listOf("book-1", "book-2"), updated.books.map { it.id })
        assertEquals(95.0, updated.books.first().price, 0.0)
        assertEquals(2, updated.books.first().stock)
    }

    @Test
    fun invalidDocumentIsSkippedAndReported() = runBlocking {
        dataSource.emit(
            bookDocument("book-1"),
            bookDocument("book-roto", validBookFields(price = "no es precio"))
        )

        val result = repository.observeCatalog().take(1).toList().single()

        result as CatalogResult.Available
        assertEquals(listOf("book-1"), result.books.map { it.id })
        assertEquals(listOf("book-roto"), result.invalidDocumentIds)
    }

    @Test
    fun onlyInvalidDocumentsAreNotReportedAsEmptyCatalog() = runBlocking {
        dataSource.emit(
            bookDocument("book-a", validBookFields(stock = 1.5)),
            bookDocument("book-b", fields = null)
        )

        val result = repository.observeCatalog().take(1).toList().single()

        assertEquals(CatalogResult.InvalidDocuments(listOf("book-a", "book-b")), result)
    }

    @Test
    fun emptyCollectionFromServerIsEmptyCatalog() = runBlocking {
        dataSource.emit()

        val result = repository.observeCatalog().take(1).toList().single()

        assertEquals(CatalogResult.Empty, result)
    }

    @Test
    fun connectionErrorEmitsReadError() = runBlocking {
        dataSource.fail(CatalogReadException(CatalogReadError.CONNECTION))

        val result = repository.observeCatalog().take(1).toList().single()

        assertEquals(CatalogResult.ReadError(CatalogReadError.CONNECTION), result)
    }

    @Test
    fun ioExceptionIsTreatedAsConnectionError() = runBlocking {
        dataSource.fail(IOException("sin red"))

        val result = repository.observeCatalog().take(1).toList().single()

        assertEquals(CatalogResult.ReadError(CatalogReadError.CONNECTION), result)
    }

    @Test
    fun offlineWithoutCacheIsConnectionErrorAndRecoversWhenServerAnswers() = runBlocking {
        dataSource.emit(isFromCache = true)
        dataSource.emit(bookDocument("book-1"))

        val results = repository.observeCatalog().take(2).toList()

        assertEquals(CatalogResult.ReadError(CatalogReadError.CONNECTION), results[0])
        assertTrue(results[1] is CatalogResult.Available)
    }

    @Test
    fun errorAfterInitialLoadIsEmittedWithoutLosingPreviousResult() = runBlocking {
        dataSource.emit(bookDocument("book-1"))
        dataSource.fail(CatalogReadException(CatalogReadError.PERMISSION_DENIED))

        val results = repository.observeCatalog().toList()

        assertTrue(results[0] is CatalogResult.Available)
        assertEquals(CatalogResult.ReadError(CatalogReadError.PERMISSION_DENIED), results[1])
    }

    @Test
    fun unexpectedErrorIsUnknownReadError() = runBlocking {
        dataSource.fail(IllegalStateException("fallo inesperado"))

        val result = repository.observeCatalog().take(1).toList().single()

        assertEquals(CatalogResult.ReadError(CatalogReadError.UNKNOWN), result)
    }
}

private class FakeBookRemoteDataSource : BookRemoteDataSource {
    private val snapshots = Channel<RemoteBookSnapshot>(Channel.UNLIMITED)

    fun emit(vararg documents: RemoteBookDocument, isFromCache: Boolean = false) {
        snapshots.trySend(RemoteBookSnapshot(documents.toList(), isFromCache))
    }

    fun fail(error: Throwable) {
        snapshots.close(error)
    }

    override fun observeBookDocuments(): Flow<RemoteBookSnapshot> = snapshots.receiveAsFlow()
}
