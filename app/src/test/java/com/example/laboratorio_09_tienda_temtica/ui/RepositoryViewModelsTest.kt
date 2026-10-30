package com.example.laboratorio_09_tienda_temtica.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CartItem
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogReadError
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogResult
import com.example.laboratorio_09_tienda_temtica.ui.orders.OrdersViewModel
import com.example.laboratorio_09_tienda_temtica.ui.orders.OrdersUiState
import com.example.laboratorio_09_tienda_temtica.ui.orders.OrderDetailUiState
import com.example.laboratorio_09_tienda_temtica.ui.state.SubmitOrderUiState
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryViewModelsTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val auth = FakeAuthRepository()
    private val books = FakeBookRepository()
    private val favorites = FakeFavoritesRepository()
    private val cart = FakeCartRepository()
    private val preferences = FakeCatalogPreferencesRepository()
    private val remote = FakeRemoteOrdersRepository()
    private val sample = Books("book-1", "Zeta", "Description", 10.25, "author-1", "Details", "Genre", "Format", 8, "https://example.com/book")

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { store.clear(); Dispatchers.resetMain() }
    private fun <T : ViewModel> track(vm: T): T { store.put(vm.javaClass.name, vm); return vm }
    private fun vm() = track(StoreViewModel(auth, books, favorites, cart, preferences, remote))

    @Test fun initialLoadingAndAuthenticationStartsOneSubscription() = runTest(dispatcher) {
        val vm = vm()
        assertEquals(CatalogStatus.Loading, vm.uiState.value.catalogStatus)
        runCurrent()
        assertEquals(1, auth.calls); assertEquals(1, books.active)
    }

    @Test fun failedAuthenticationDoesNotListenAndRetryRecovers() = runTest(dispatcher) {
        auth.result = Result.failure(IllegalStateException("private detail"))
        val vm = vm(); runCurrent()
        assertTrue(vm.uiState.value.catalogStatus is CatalogStatus.Error)
        assertEquals(0, books.subscriptions)
        auth.result = Result.success(Unit)
        vm.retryCatalog(); runCurrent()
        assertEquals(2, auth.calls); assertEquals(1, books.active)
        books.results.emit(CatalogResult.Available(listOf(sample))); runCurrent()
        assertTrue(vm.uiState.value.catalogStatus is CatalogStatus.Ready)
    }

    @Test fun emptyInvalidAndFirestoreErrorsRemainDistinct() = runTest(dispatcher) {
        val vm = vm(); runCurrent()
        books.results.emit(CatalogResult.Empty); runCurrent()
        assertEquals(CatalogStatus.Empty, vm.uiState.value.catalogStatus)
        books.results.emit(CatalogResult.InvalidDocuments(listOf("bad"))); runCurrent()
        assertEquals(CatalogStatus.InvalidDocuments(1), vm.uiState.value.catalogStatus)
        books.results.emit(CatalogResult.ReadError(CatalogReadError.CONNECTION)); runCurrent()
        assertTrue(vm.uiState.value.catalogStatus is CatalogStatus.Error)
    }

    @Test fun liveUpdatesPreserveIdsFavoritesCartAndExactStock() = runTest(dispatcher) {
        favorites.ids.value = listOf(sample.id); cart.items.value = listOf(CartItem(sample.id, 2))
        val vm = vm(); runCurrent()
        books.results.emit(CatalogResult.Available(listOf(sample), listOf("bad"))); runCurrent()
        assertEquals(CatalogStatus.Ready(1), vm.uiState.value.catalogStatus)
        books.results.emit(CatalogResult.Available(listOf(sample.copy(stock = 3)))); runCurrent()
        assertEquals(3, vm.findBookById(sample.id)?.stock)
        assertTrue(sample.id in vm.uiState.value.favoriteBookIds)
        assertEquals(2, vm.uiState.value.orderLines.single().quantity)
    }

    @Test fun searchAndOrderingAreLocalWithoutExtraSubscriptions() = runTest(dispatcher) {
        val vm = vm(); runCurrent()
        books.results.emit(CatalogResult.Available(listOf(sample, sample.copy(id = "book-2", title = "Alpha", price = 20.0)))); runCurrent()
        assertEquals("Alpha", vm.uiState.value.filteredBooks.first().title)
        vm.updateSortOrder(CatalogSortOrder.PRICE); runCurrent()
        assertEquals(sample.id, vm.uiState.value.filteredBooks.first().id)
        vm.updateSearchQuery("zEtA"); runCurrent()
        assertEquals(listOf(sample.id), vm.uiState.value.filteredBooks.map { it.id })
        assertEquals(1, books.subscriptions)
    }

    @Test fun retriesCancelPreviousSubscriptionAndRetainLatestCart() = runTest(dispatcher) {
        val vm = vm(); runCurrent()
        cart.items.value = listOf(CartItem(sample.id, 1))
        books.results.emit(CatalogResult.Available(listOf(sample))); runCurrent()
        books.results.emit(CatalogResult.ReadError(CatalogReadError.CONNECTION)); runCurrent()
        assertEquals(1, vm.uiState.value.orderLines.size)
        repeat(3) { vm.retryCatalog(); runCurrent() }
        assertEquals(1, books.maximumActive); assertEquals(1, books.active)
        books.results.emit(CatalogResult.Available(listOf(sample))); runCurrent()
        assertTrue(vm.uiState.value.catalogStatus is CatalogStatus.Ready)
        store.clear(); runCurrent(); assertEquals(0, books.active)
    }

    private suspend fun TestScope.prepareCheckout(vm: StoreViewModel) {
        cart.items.value = listOf(CartItem(sample.id, 2))
        books.results.emit(CatalogResult.Available(listOf(sample))); runCurrent()
        vm.updateFullName("Ana Lopez"); vm.updatePhone("12345678")
    }

    @Test fun successfulSubmissionUsesSnapshotThenClearsCart() = runTest(dispatcher) {
        val vm = vm(); runCurrent(); prepareCheckout(vm)
        vm.confirmOrder(); runCurrent()
        assertTrue(vm.submitOrderUiState.value is SubmitOrderUiState.Success)
        assertTrue(cart.items.value.isEmpty())
        assertEquals(BigDecimal("20.50"), remote.submitted?.total)
        assertEquals(sample.id, remote.submitted?.lines?.single()?.bookId)
    }

    @Test fun failedSubmissionKeepsCartAndCheckout() = runTest(dispatcher) {
        remote.failCreate = true
        val vm = vm(); runCurrent(); prepareCheckout(vm)
        vm.confirmOrder(); runCurrent()
        assertTrue(vm.submitOrderUiState.value is SubmitOrderUiState.Error)
        assertEquals(2, cart.items.value.single().quantity)
        assertEquals("Ana Lopez", vm.checkoutUiState.value.fullName)
    }

    @Test fun ordersListAndMissingDetailUseDomainResults() = runTest(dispatcher) {
        val vm = track(OrdersViewModel(remote))
        remote.orders = listOf(RemoteOrder("1", "2026-10-29T00:00:00Z", "CF", BigDecimal("1.00"), emptyList(), "CASH_ON_DELIVERY"))
        vm.loadOrders(); runCurrent()
        assertEquals(remote.orders, (vm.ordersUiState.value as OrdersUiState.Success).orders)
        vm.loadOrderDetail("missing"); runCurrent()
        assertEquals(OrderDetailUiState.NotFound, vm.orderDetailUiState.value)
        vm.loadOrderDetail("1"); runCurrent()
        assertTrue(vm.orderDetailUiState.value is OrderDetailUiState.Success)
        remote.orders = emptyList(); vm.loadOrders(); runCurrent()
        assertEquals(OrdersUiState.Empty, vm.ordersUiState.value)
    }
}
