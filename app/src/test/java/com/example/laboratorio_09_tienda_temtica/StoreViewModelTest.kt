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

        assertEquals(3, state.books.size)
        assertEquals(2, state.authors.size)
        assertTrue(state.books.all { book -> state.authors.any { it.id == book.authorId } })
        assertTrue(state.favoriteBookIds.isEmpty())
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
