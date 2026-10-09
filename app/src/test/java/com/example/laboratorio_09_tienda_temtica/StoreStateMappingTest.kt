package com.example.laboratorio_09_tienda_temtica

import com.example.laboratorio_09_tienda_temtica.data.local.OrderLineEntity
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.OrderLine
import com.example.laboratorio_09_tienda_temtica.model.OrderResult
import com.example.laboratorio_09_tienda_temtica.model.formatQuetzales
import com.example.laboratorio_09_tienda_temtica.ui.BillingType
import com.example.laboratorio_09_tienda_temtica.ui.CheckoutUiState
import com.example.laboratorio_09_tienda_temtica.ui.PaymentMethod
import com.example.laboratorio_09_tienda_temtica.ui.buildStoreUiState
import com.example.laboratorio_09_tienda_temtica.ui.calculateOrderTotal
import com.example.laboratorio_09_tienda_temtica.ui.filterAndSortBooks
import com.example.laboratorio_09_tienda_temtica.ui.generateOrderFolio
import com.example.laboratorio_09_tienda_temtica.ui.validateOrderAddition
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreStateMappingTest {

    @Test
    fun filterIgnoresCaseAndOuterSpaces() {
        val result = filterAndSortBooks(
            books = books,
            query = "  BETA  ",
            sortOrder = CatalogSortOrder.NAME
        )

        assertEquals(listOf("Beta"), result.map { book -> book.title })
    }

    @Test
    fun emptyQuerySortsByName() {
        val result = filterAndSortBooks(books, "", CatalogSortOrder.NAME)

        assertEquals(listOf("Alpha", "Beta", "Gamma"), result.map { book -> book.title })
    }

    @Test
    fun emptyQuerySortsByPrice() {
        val result = filterAndSortBooks(books, "", CatalogSortOrder.PRICE)

        assertEquals(listOf("Gamma", "Alpha", "Beta"), result.map { book -> book.title })
    }

    @Test
    fun queryWithoutMatchesReturnsEmptyList() {
        val result = filterAndSortBooks(books, "missing", CatalogSortOrder.NAME)

        assertTrue(result.isEmpty())
    }

    @Test
    fun persistedEntitiesBuildCompleteUiState() {
        val state = buildStoreUiState(
            books = books,
            authors = emptyList(),
            searchQuery = "",
            sortOrder = CatalogSortOrder.PRICE,
            favoriteBookIds = listOf("book-a", "book-a", "book-c"),
            persistedOrderLines = listOf(
                OrderLineEntity(bookId = "book-a", quantity = 2),
                OrderLineEntity(bookId = "book-c", quantity = 1)
            )
        )

        assertEquals(setOf("book-a", "book-c"), state.favoriteBookIds)
        assertEquals(CatalogSortOrder.PRICE, state.sortOrder)
        assertEquals(listOf("book-c", "book-a", "book-b"), state.filteredBooks.map { it.id })
        assertEquals(3, state.orderUnitCount)
        assertEquals(BigDecimal("25.50"), state.orderTotal)
    }

    @Test
    fun missingBooksAndNonPositiveQuantitiesAreNotRestored() {
        val state = buildStoreUiState(
            books = books,
            authors = emptyList(),
            searchQuery = "",
            sortOrder = CatalogSortOrder.NAME,
            favoriteBookIds = emptyList(),
            persistedOrderLines = listOf(
                OrderLineEntity(bookId = "missing", quantity = 1),
                OrderLineEntity(bookId = "book-a", quantity = 0)
            )
        )

        assertTrue(state.orderLines.isEmpty())
        assertEquals(BigDecimal("0.00"), state.orderTotal)
    }

    @Test
    fun orderValidationRejectsMissingBookInvalidQuantityAndExcessStock() {
        assertEquals(OrderResult.BOOK_NOT_FOUND, validateOrderAddition(null, 0, 1))
        assertEquals(OrderResult.INVALID_QUANTITY, validateOrderAddition(books[0], 0, 0))
        assertEquals(OrderResult.INSUFFICIENT_STOCK, validateOrderAddition(books[1], 1, 2))
        assertEquals(OrderResult.ADDED, validateOrderAddition(books[1], 0, 2))
    }

    @Test
    fun totalsKeepTwoDecimalPlaces() {
        val lines = listOf(
            OrderLine("book-a", "Alpha", BigDecimal("10.25"), 2),
            OrderLine("book-c", "Gamma", BigDecimal("5.00"), 1)
        )

        assertEquals(BigDecimal("25.50"), calculateOrderTotal(lines))
        assertEquals("Q 25.50", formatQuetzales(calculateOrderTotal(lines)))
    }

    @Test
    fun storedSortValuesHaveSafeDefault() {
        assertEquals(CatalogSortOrder.NAME, CatalogSortOrder.fromStoredValue(null))
        assertEquals(CatalogSortOrder.NAME, CatalogSortOrder.fromStoredValue("unexpected"))
        assertEquals(CatalogSortOrder.PRICE, CatalogSortOrder.fromStoredValue("price"))
    }

    @Test
    fun checkoutValidityStillUsesBillingRules() {
        val validCf = CheckoutUiState(
            fullName = "María Morales",
            phone = "55123456"
        )
        val invalidNit = validCf.copy(billingType = BillingType.NIT)
        val validNit = invalidNit.copy(
            nit = "12345",
            businessName = "Editorial Sol",
            paymentMethod = PaymentMethod.BANK_TRANSFER
        )

        assertTrue(validCf.isFormValid)
        assertFalse(invalidNit.isFormValid)
        assertTrue(validNit.isFormValid)
        assertNull(validNit.nitError)
    }

    @Test
    fun folioGeneratorRemainsDeterministic() {
        assertEquals("#ORD-00001", generateOrderFolio(1))
        assertEquals("#ORD-00123", generateOrderFolio(123))
    }

    private companion object {
        val books = listOf(
            book(id = "book-b", title = "Beta", price = 20.00, stock = 4),
            book(id = "book-a", title = "Alpha", price = 10.25, stock = 2),
            book(id = "book-c", title = "Gamma", price = 5.00, stock = 5)
        )

        fun book(
            id: String,
            title: String,
            price: Double,
            stock: Int
        ): Books = Books(
            id = id,
            title = title,
            description = "Description",
            price = price,
            authorId = "author",
            details = "Details",
            genre = "Genre",
            format = "Format",
            stock = stock,
            imageUrl = "https://picsum.photos/seed/$id/320/480"
        )
    }
}
