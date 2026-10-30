package com.example.laboratorio_09_tienda_temtica.ui

import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.OrderLine
import java.math.BigDecimal

data class StoreUiState(
    val catalogStatus: CatalogStatus = CatalogStatus.Loading,
    val books: List<Books> = emptyList(),
    val filteredBooks: List<Books> = emptyList(),
    val authors: List<AuthorProfile> = emptyList(),
    val favoriteBookIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val sortOrder: CatalogSortOrder = CatalogSortOrder.NAME,
    val orderLines: List<OrderLine> = emptyList(),
    val orderTotal: BigDecimal = BigDecimal.ZERO.setScale(2),
    val orderUnitCount: Int = 0
)

sealed interface CatalogStatus {
    data object Loading : CatalogStatus

    /** [skippedDocumentCount] books were left out because their documents are defective. */
    data class Ready(val skippedDocumentCount: Int = 0) : CatalogStatus

    data object Empty : CatalogStatus

    data class InvalidDocuments(val documentCount: Int) : CatalogStatus

    data class Error(val message: String) : CatalogStatus
}
