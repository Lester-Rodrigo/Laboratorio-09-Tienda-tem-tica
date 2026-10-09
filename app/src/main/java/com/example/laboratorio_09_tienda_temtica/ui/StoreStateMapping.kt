package com.example.laboratorio_09_tienda_temtica.ui

import com.example.laboratorio_09_tienda_temtica.data.local.OrderLineEntity
import com.example.laboratorio_09_tienda_temtica.data.preferences.sortBooks
import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.OrderLine
import com.example.laboratorio_09_tienda_temtica.model.OrderResult
import com.example.laboratorio_09_tienda_temtica.model.toMoney
import java.math.BigDecimal
import java.math.RoundingMode

internal fun buildStoreUiState(
    books: List<Books>,
    authors: List<AuthorProfile>,
    searchQuery: String,
    sortOrder: CatalogSortOrder,
    favoriteBookIds: List<String>,
    persistedOrderLines: List<OrderLineEntity>
): StoreUiState {
    val orderLines = persistedOrderLines.mapNotNull { entity ->
        books.firstOrNull { book -> book.id == entity.bookId }
            ?.takeIf { entity.quantity > 0 }
            ?.let { book ->
                OrderLine(
                    bookId = book.id,
                    title = book.title,
                    unitPrice = book.price.toMoney(),
                    quantity = entity.quantity
                )
            }
    }
    val matchingBooks = filterAndSortBooks(
        books = books,
        query = searchQuery,
        sortOrder = sortOrder
    )

    return StoreUiState(
        books = books,
        filteredBooks = matchingBooks,
        authors = authors,
        favoriteBookIds = favoriteBookIds.toSet(),
        searchQuery = searchQuery,
        sortOrder = sortOrder,
        orderLines = orderLines,
        orderTotal = calculateOrderTotal(orderLines),
        orderUnitCount = orderLines.sumOf { line -> line.quantity }
    )
}

internal fun filterAndSortBooks(
    books: List<Books>,
    query: String,
    sortOrder: CatalogSortOrder
): List<Books> {
    val normalizedQuery = query.trim()
    val matchingBooks = if (normalizedQuery.isEmpty()) {
        books
    } else {
        books.filter { book ->
            book.title.contains(normalizedQuery, ignoreCase = true)
        }
    }
    return sortBooks(matchingBooks, sortOrder)
}

internal fun validateOrderAddition(
    book: Books?,
    currentQuantity: Int,
    quantityToAdd: Int
): OrderResult = when {
    book == null -> OrderResult.BOOK_NOT_FOUND
    quantityToAdd <= 0 -> OrderResult.INVALID_QUANTITY
    currentQuantity + quantityToAdd > book.stock -> OrderResult.INSUFFICIENT_STOCK
    else -> OrderResult.ADDED
}

internal fun calculateOrderTotal(lines: List<OrderLine>): BigDecimal =
    lines.fold(BigDecimal.ZERO) { total, line -> total + line.subtotal }
        .setScale(2, RoundingMode.HALF_UP)
