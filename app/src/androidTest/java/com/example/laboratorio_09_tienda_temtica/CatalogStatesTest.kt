package com.example.laboratorio_09_tienda_temtica

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.ui.CatalogStatus
import com.example.laboratorio_09_tienda_temtica.ui.screens.CatalogScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CatalogStatesTest {
    @get:Rule val compose = createComposeRule()

    private fun render(status: CatalogStatus, retry: () -> Unit = {}) {
        compose.setContent {
            MaterialTheme {
                CatalogScreen(
                    catalogStatus = status, onRetryCatalog = retry,
                    books = emptyList(), favoriteBookIds = emptySet(), searchQuery = "",
                    sortOrder = CatalogSortOrder.NAME, onSearchQueryChange = {}, onClearSearch = {},
                    onSortOrderChange = {}, onBookClick = {}, onFavoriteClick = {},
                    orderUnitCount = 0, onOrderClick = {}, onMyOrdersClick = {}
                )
            }
        }
    }

    @Test fun loadingHasAccessibleIndicator() {
        render(CatalogStatus.Loading)
        compose.onNodeWithContentDescription("Cargando catálogo").assertIsDisplayed()
    }

    @Test fun emptyCatalogIsNotSearchWithoutMatches() {
        render(CatalogStatus.Empty)
        compose.onNodeWithText("No hay libros disponibles").assertIsDisplayed()
    }

    @Test fun errorOffersRetryCallback() {
        var retries = 0
        render(CatalogStatus.Error("No se pudo leer el catálogo.")) { retries++ }
        compose.onNodeWithText("Reintentar").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }
}
