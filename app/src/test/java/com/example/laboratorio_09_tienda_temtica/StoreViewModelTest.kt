package com.example.laboratorio_09_tienda_temtica

import com.example.laboratorio_09_tienda_temtica.ui.StoreViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelTest {

    @Test
    fun initialStateContainsRequiredCatalogData() {
        val state = StoreViewModel().uiState.value

        assertEquals(500, state.books.size)
        assertEquals(500, state.filteredBooks.size)
        assertEquals(6, state.authors.size)
        assertTrue(state.books.all { book -> state.authors.any { it.id == book.authorId } })
        assertTrue(state.favoriteBookIds.isEmpty())
    }

    @Test
    fun emptyQueryReturnsCompleteCatalog() {
        val viewModel = StoreViewModel()
        viewModel.updateSearchQuery("")
        assertEquals(500, viewModel.uiState.value.filteredBooks.size)
    }

    @Test
    fun exactQueryReturnsMatchingTitle() {
        val viewModel = StoreViewModel()
        viewModel.updateSearchQuery("El principito")
        assertEquals(listOf("El principito"), viewModel.uiState.value.filteredBooks.map { it.title })
    }

    @Test
    fun partialQueryReturnsMatchingTitles() {
        val viewModel = StoreViewModel()
        viewModel.updateSearchQuery("princip")
        assertTrue(viewModel.uiState.value.filteredBooks.any { it.title == "El principito" })
        assertTrue(viewModel.uiState.value.filteredBooks.all { it.title.contains("princip", ignoreCase = true) })
    }

    @Test
    fun queryIgnoresLetterCase() {
        val viewModel = StoreViewModel()
        viewModel.updateSearchQuery("EL PRINCIPITO")
        assertEquals(listOf("El principito"), viewModel.uiState.value.filteredBooks.map { it.title })
    }

    @Test
    fun queryIgnoresOuterSpacesWithoutChangingEnteredText() {
        val viewModel = StoreViewModel()
        val query = "  El principito  "
        viewModel.updateSearchQuery(query)
        assertEquals(query, viewModel.uiState.value.searchQuery)
        assertEquals(listOf("El principito"), viewModel.uiState.value.filteredBooks.map { it.title })
    }

    @Test
    fun queryWithoutMatchesReturnsEmptyList() {
        val viewModel = StoreViewModel()
        viewModel.updateSearchQuery("título que no existe")
        assertTrue(viewModel.uiState.value.filteredBooks.isEmpty())
    }

    @Test
    fun clearQueryRestoresCompleteCatalog() {
        val viewModel = StoreViewModel()
        viewModel.updateSearchQuery("El principito")
        viewModel.clearSearchQuery()
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertEquals(500, viewModel.uiState.value.filteredBooks.size)
    }

    @Test
    fun resolvesBookById() {
        val viewModel = StoreViewModel()
        assertEquals("Cien años de soledad", viewModel.findBookById("book-2")?.title)
    }

    @Test
    fun missingBookIdReturnsNull() {
        val viewModel = StoreViewModel()
        assertEquals(null, viewModel.findBookById("missing-book"))
    }

    @Test
    fun orderIsAcceptedWhenBookHasStock() {
        val viewModel = StoreViewModel()
        assertTrue(viewModel.addToOrder("book-1"))
    }

    @Test
    fun orderIsRejectedWhenBookHasNoStock() {
        val viewModel = StoreViewModel()
        assertFalse(viewModel.addToOrder("book-3"))
    }

    @Test
    fun toggleFavoriteAddsAndRemovesBookWithoutChangingCatalog() {
        val viewModel = StoreViewModel()
        val initialBooks = viewModel.uiState.value.books

        viewModel.toggleFavorite("book-2")
        assertTrue("book-2" in viewModel.uiState.value.favoriteBookIds)
        assertEquals(initialBooks, viewModel.uiState.value.books)

        viewModel.toggleFavorite("book-2")
        assertFalse("book-2" in viewModel.uiState.value.favoriteBookIds)
    }

    @Test
    fun unknownBookCannotBecomeFavorite() {
        val viewModel = StoreViewModel()

        viewModel.toggleFavorite("missing-book")

        assertTrue(viewModel.uiState.value.favoriteBookIds.isEmpty())
    }
}
