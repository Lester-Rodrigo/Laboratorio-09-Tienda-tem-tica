package com.example.laboratorio_09_tienda_temtica

import com.example.laboratorio_09_tienda_temtica.model.OrderResult
import com.example.laboratorio_09_tienda_temtica.model.formatQuetzales
import com.example.laboratorio_09_tienda_temtica.ui.BillingType
import com.example.laboratorio_09_tienda_temtica.ui.CheckoutUiState
import com.example.laboratorio_09_tienda_temtica.ui.PaymentMethod
import com.example.laboratorio_09_tienda_temtica.ui.StoreViewModel
import com.example.laboratorio_09_tienda_temtica.ui.generateOrderFolio
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelTest {

    @Test
    fun checkoutInitialStateUsesCfAndCashWithUntouchedInvalidFields() {
        val state = StoreViewModel().checkoutUiState.value

        assertEquals(BillingType.CF, state.billingType)
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, state.paymentMethod)
        assertFalse(state.isFullNameTouched)
        assertFalse(state.isPhoneTouched)
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)
        assertEquals(null, state.visibleFullNameError)
        assertEquals(null, state.visiblePhoneError)
        assertFalse(state.isFormValid)
    }

    @Test
    fun editingAndCorrectingCheckoutFieldsUpdatesVisibleErrors() {
        val viewModel = StoreViewModel()

        viewModel.updateFullName("A2")
        viewModel.updatePhone("123")
        assertEquals("El nombre no puede contener números.", viewModel.checkoutUiState.value.visibleFullNameError)
        assertEquals("Ingresa exactamente 8 dígitos.", viewModel.checkoutUiState.value.visiblePhoneError)

        viewModel.updateFullName("María José")
        viewModel.updatePhone("55123456")
        assertEquals(null, viewModel.checkoutUiState.value.visibleFullNameError)
        assertEquals(null, viewModel.checkoutUiState.value.visiblePhoneError)
        assertTrue(viewModel.checkoutUiState.value.isFormValid)
    }

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
        assertEquals(OrderResult.ADDED, viewModel.addToOrder("book-1"))
        assertEquals(1, viewModel.uiState.value.orderLines.size)
        assertEquals(1, viewModel.uiState.value.orderUnitCount)
    }

    @Test
    fun orderIsRejectedWhenBookHasNoStock() {
        val viewModel = StoreViewModel()
        assertEquals(OrderResult.INSUFFICIENT_STOCK, viewModel.addToOrder("book-3"))
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
    }

    @Test
    fun sameBookAccumulatesInSingleLine() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-1")
        viewModel.addToOrder("book-1")
        viewModel.addToOrder("book-1", quantity = 2)

        val lines = viewModel.uiState.value.orderLines
        assertEquals(1, lines.size)
        assertEquals(4, lines.single().quantity)
    }

    @Test
    fun invalidOperationsDoNotModifyOrder() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-2")
        val before = viewModel.uiState.value

        assertEquals(OrderResult.BOOK_NOT_FOUND, viewModel.addToOrder("missing-book"))
        assertEquals(OrderResult.INVALID_QUANTITY, viewModel.addToOrder("book-2", quantity = 0))
        assertEquals(OrderResult.INVALID_QUANTITY, viewModel.addToOrder("book-2", quantity = -3))
        assertEquals(OrderResult.INSUFFICIENT_STOCK, viewModel.addToOrder("book-2", quantity = 3))
        viewModel.decreaseQuantity("missing-book")
        viewModel.removeFromOrder("missing-book")

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun cannotExceedStockWhenIncreasing() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-2")
        assertEquals(OrderResult.ADDED, viewModel.increaseQuantity("book-2"))
        assertEquals(OrderResult.ADDED, viewModel.increaseQuantity("book-2"))
        assertEquals(OrderResult.INSUFFICIENT_STOCK, viewModel.increaseQuantity("book-2"))
        assertEquals(3, viewModel.uiState.value.orderLines.single().quantity)
    }

    @Test
    fun increaseRequiresExistingLine() {
        val viewModel = StoreViewModel()
        assertEquals(OrderResult.BOOK_NOT_FOUND, viewModel.increaseQuantity("book-1"))
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
    }

    @Test
    fun decreaseToZeroRemovesLine() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-1", quantity = 2)
        viewModel.decreaseQuantity("book-1")
        assertEquals(1, viewModel.uiState.value.orderLines.single().quantity)

        viewModel.decreaseQuantity("book-1")
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
        assertEquals(0, viewModel.uiState.value.orderUnitCount)
    }

    @Test
    fun removeDeletesOnlySelectedLine() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-1")
        viewModel.addToOrder("book-2")
        viewModel.removeFromOrder("book-1")
        assertEquals(listOf("book-2"), viewModel.uiState.value.orderLines.map { it.bookId })
    }

    @Test
    fun subtotalsAndTotalUseTwoDecimals() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-1", quantity = 3)
        viewModel.addToOrder("book-2", quantity = 2)

        val state = viewModel.uiState.value
        assertEquals(BigDecimal("269.70"), state.orderLines[0].subtotal)
        assertEquals(BigDecimal("299.00"), state.orderLines[1].subtotal)
        assertEquals(BigDecimal("568.70"), state.orderTotal)
        assertEquals(5, state.orderUnitCount)
        assertEquals("Q 568.70", formatQuetzales(state.orderTotal))
    }

    @Test
    fun emptyOrderHasZeroTotal() {
        val state = StoreViewModel().uiState.value
        assertTrue(state.orderLines.isEmpty())
        assertEquals("Q 0.00", formatQuetzales(state.orderTotal))
        assertEquals(0, state.orderUnitCount)
    }

    @Test
    fun orderDoesNotChangeCatalogStock() {
        val viewModel = StoreViewModel()
        val initialBooks = viewModel.uiState.value.books
        viewModel.addToOrder("book-1", quantity = 2)
        assertEquals(initialBooks, viewModel.uiState.value.books)
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

    @Test
    fun switchingToCfClearsNitAndBusinessNameAndExcludesThemFromValidity() {
        val viewModel = StoreViewModel()
        viewModel.updateBillingType(BillingType.NIT)
        viewModel.updateNit("123")
        viewModel.updateBusinessName("Ab")

        viewModel.updateBillingType(BillingType.CF)

        val state = viewModel.checkoutUiState.value
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)
        assertEquals(null, state.nitError)
        assertEquals(null, state.businessNameError)
        assertEquals(null, state.visibleNitError)
        assertEquals(null, state.visibleBusinessNameError)
    }

    @Test
    fun switchingBackToNitKeepsFiscalValuesButRequiresThemAgain() {
        val viewModel = StoreViewModel()
        viewModel.updateFullName("María Morales")
        viewModel.updatePhone("55123456")
        viewModel.updateBillingType(BillingType.NIT)
        viewModel.updateNit("123")
        viewModel.updateBusinessName("Ab")

        viewModel.updateBillingType(BillingType.CF)
        assertTrue(viewModel.checkoutUiState.value.isFormValid)

        viewModel.updateBillingType(BillingType.NIT)
        val state = viewModel.checkoutUiState.value
        assertEquals("123", state.nit)
        assertEquals("Ab", state.businessName)
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)
        assertFalse(state.isFormValid)
        assertEquals(null, state.visibleNitError)
        assertEquals(null, state.visibleBusinessNameError)
    }

    @Test
    fun switchingBackToNitValidatesEmptyFieldsWithoutShowingErrorsUntilTouched() {
        val viewModel = StoreViewModel()
        viewModel.updateBillingType(BillingType.NIT)
        viewModel.updateBillingType(BillingType.CF)

        viewModel.updateBillingType(BillingType.NIT)

        val state = viewModel.checkoutUiState.value
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)
        assertEquals("Ingresa al menos 5 dígitos.", state.nitError)
        assertEquals(null, state.visibleNitError)
        assertEquals(null, state.visibleBusinessNameError)
        assertFalse(state.isFormValid)
    }

    @Test
    fun isFormValidIsComputedEvenWhenFieldsAreNotTouched() {
        val viewModel = StoreViewModel()

        assertFalse(viewModel.checkoutUiState.value.isFormValid)

        viewModel.updateFullName("María Morales")
        viewModel.updatePhone("55123456")

        assertTrue(viewModel.checkoutUiState.value.isFormValid)
    }

    @Test
    fun folioIncrementsWithEachConfirmedOrder() {
        val viewModel = StoreViewModel()
        viewModel.updateFullName("María Morales")
        viewModel.updatePhone("55123456")
        viewModel.addToOrder("book-1")

        assertTrue(viewModel.confirmOrder())
        assertEquals("#ORD-00001", viewModel.receipt.value?.folio)

        viewModel.updateFullName("Juan Pérez")
        viewModel.updatePhone("55987654")
        viewModel.addToOrder("book-1")

        assertTrue(viewModel.confirmOrder())
        assertEquals("#ORD-00002", viewModel.receipt.value?.folio)
    }

    @Test
    fun folioGeneratorUsesDeterministicPaddedSequence() {
        assertEquals("#ORD-00001", generateOrderFolio(1))
        assertEquals("#ORD-00002", generateOrderFolio(2))
        assertEquals("#ORD-00123", generateOrderFolio(123))
    }

    @Test
    fun validNitOrderCapturesReceiptBeforeResettingOrderAndForm() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-1", quantity = 2)
        viewModel.updateFullName("Ana López")
        viewModel.updatePhone("55123456")
        viewModel.updateBillingType(BillingType.NIT)
        viewModel.updateNit("12345")
        viewModel.updateBusinessName("Editorial Sol")
        viewModel.updatePaymentMethod(PaymentMethod.BANK_TRANSFER)

        assertTrue(viewModel.confirmOrder())

        val receipt = requireNotNull(viewModel.receipt.value)
        assertEquals("#ORD-00001", receipt.folio)
        assertEquals("Ana López", receipt.customerName)
        assertEquals("55123456", receipt.customerPhone)
        assertEquals(BillingType.NIT, receipt.billingType)
        assertEquals("12345", receipt.nit)
        assertEquals("Editorial Sol", receipt.businessName)
        assertEquals(PaymentMethod.BANK_TRANSFER, receipt.paymentMethod)
        assertEquals(BigDecimal("179.80"), receipt.total)
        assertEquals(0, viewModel.uiState.value.orderUnitCount)
        assertEquals(CheckoutUiState(), viewModel.checkoutUiState.value)
    }

    @Test
    fun invalidFormDoesNotDestroyExistingOrderOrAdvanceFolio() {
        val viewModel = StoreViewModel()
        viewModel.addToOrder("book-1")
        val orderBefore = viewModel.uiState.value

        assertFalse(viewModel.confirmOrder())
        assertEquals(orderBefore, viewModel.uiState.value)
        assertEquals(null, viewModel.receipt.value)

        viewModel.updateFullName("María Morales")
        viewModel.updatePhone("55123456")
        assertTrue(viewModel.confirmOrder())
        assertEquals("#ORD-00001", viewModel.receipt.value?.folio)
    }

    @Test
    fun emptyOrderDoesNotConfirmEvenWithValidForm() {
        val viewModel = StoreViewModel()
        viewModel.updateFullName("María Morales")
        viewModel.updatePhone("55123456")

        assertFalse(viewModel.confirmOrder())
        assertEquals(null, viewModel.receipt.value)
        assertTrue(viewModel.checkoutUiState.value.isFormValid)
    }

    @Test
    fun confirmedReceiptIsKeptAfterFormResetAndInvalidAttempts() {
        val viewModel = StoreViewModel()
        viewModel.updateFullName("María Morales")
        viewModel.updatePhone("55123456")
        viewModel.addToOrder("book-1")

        assertTrue(viewModel.confirmOrder())
        val receipt = viewModel.receipt.value
        assertEquals("María Morales", receipt?.customerName)
        assertEquals(viewModel.checkoutUiState.value, CheckoutUiState())

        assertFalse(viewModel.confirmOrder())
        assertEquals(receipt, viewModel.receipt.value)
    }

    @Test
    fun confirmOrderEmptiesTheOrderAndDoesNothingWhenInvalid() {
        val viewModel = StoreViewModel()

        assertFalse(viewModel.confirmOrder())
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
        assertEquals(null, viewModel.receipt.value)

        viewModel.updateFullName("María Morales")
        viewModel.updatePhone("55123456")
        viewModel.addToOrder("book-1", quantity = 2)

        assertTrue(viewModel.confirmOrder())
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
        assertEquals(0, viewModel.uiState.value.orderUnitCount)
        assertEquals(BigDecimal.ZERO.setScale(2), viewModel.uiState.value.orderTotal)
    }
}
