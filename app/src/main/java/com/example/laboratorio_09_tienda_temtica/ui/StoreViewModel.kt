package com.example.laboratorio_09_tienda_temtica.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.laboratorio_09_tienda_temtica.BookStoreApplication
import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CartItem
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.OrderResult
import com.example.laboratorio_09_tienda_temtica.model.OrderSubmission
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderLine
import com.example.laboratorio_09_tienda_temtica.model.repository.AuthRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CartRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogPreferencesRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogReadError
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogResult
import com.example.laboratorio_09_tienda_temtica.model.repository.FavoritesRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.RemoteOrdersRepository
import com.example.laboratorio_09_tienda_temtica.ui.state.SubmitOrderUiState
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel(
    private val authRepository: AuthRepository,
    private val bookRepository: BookRepository,
    private val favoritesRepository: FavoritesRepository,
    private val cartRepository: CartRepository,
    private val catalogPreferencesRepository: CatalogPreferencesRepository,
    private val remoteOrdersRepository: RemoteOrdersRepository
) : ViewModel() {
    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()
    fun dismissOperationError() {
        _operationError.value = null
    }
    private fun reportPersistenceResult(result: Result<Unit>) {
        if (result.isFailure) _operationError.value = "No se pudo guardar el cambio. Inténtalo nuevamente."
    }
    private val authors = listOf(
        AuthorProfile(
            id = "author-1",
            name = "Antoine de Saint-Exupéry",
            role = "Escritor y aviador",
            location = "Lyon, Francia",
            description = "Autor francés cuya experiencia como aviador inspiró relatos sobre la aventura, la responsabilidad y los vínculos humanos."
        ),
        AuthorProfile(
            id = "author-2",
            name = "Gabriel García Márquez",
            role = "Escritor y periodista",
            location = "Aracataca, Colombia",
            description = "Novelista colombiano y Premio Nobel de Literatura, reconocido como una figura esencial del realismo mágico latinoamericano."
        ),
        AuthorProfile(
            id = "author-3",
            name = "Lucía Herrera",
            role = "Novelista",
            location = "Ciudad de Guatemala, Guatemala",
            description = "Autora de ficción contemporánea, misterio y relatos inspirados en la memoria de las ciudades."
        ),
        AuthorProfile(
            id = "author-4",
            name = "Mateo Salvatierra",
            role = "Escritor de aventuras",
            location = "Quetzaltenango, Guatemala",
            description = "Escritor de aventuras y fantasía cuyos personajes exploran paisajes y leyendas de América Latina."
        ),
        AuthorProfile(
            id = "author-5",
            name = "Elena Robles",
            role = "Historiadora y ensayista",
            location = "Puebla, México",
            description = "Investigadora dedicada a convertir episodios históricos en relatos accesibles para nuevos lectores."
        ),
        AuthorProfile(
            id = "author-6",
            name = "Nicolás Vega",
            role = "Autor de ciencia ficción",
            location = "San José, Costa Rica",
            description = "Autor interesado en la relación entre tecnología, identidad y futuros posibles."
        )
    )
    private val searchQuery = MutableStateFlow("")
    private var latestBooks: List<Books> = emptyList()
    private val catalogRetryCount = MutableStateFlow(0)

    // Each retry starts a new subscription; null marks the loading state until the first result.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val catalogResult: Flow<CatalogResult?> = catalogRetryCount.flatMapLatest {
        flow<CatalogResult?> {
            emit(null)
            if (authRepository.ensureAnonymousSession().isSuccess) {
                emitAll(bookRepository.observeCatalog())
            } else {
                emit(CatalogResult.ReadError(CatalogReadError.PERMISSION_DENIED))
            }
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(CatalogResult.ReadError(CatalogReadError.UNKNOWN))
        }
    }
    private val initialUiState = buildStoreUiState(
        books = emptyList(),
        authors = authors,
        searchQuery = "",
        sortOrder = CatalogSortOrder.NAME,
        favoriteBookIds = emptyList(),
        persistedOrderLines = emptyList(),
        catalogStatus = CatalogStatus.Loading
    )
    val uiState: StateFlow<StoreUiState> = combine(
        searchQuery,
        catalogResult,
        favoritesRepository.observeFavoriteBookIds(),
        cartRepository.observeItems(),
        catalogPreferencesRepository.observeSortOrder()
    ) { query, catalog, favoriteIds, cartItems, sortOrder ->
        latestBooks = when (catalog) {
            is CatalogResult.Available -> catalog.books
            CatalogResult.Empty, is CatalogResult.InvalidDocuments -> emptyList()
            null, is CatalogResult.ReadError -> latestBooks
        }
        buildStoreUiState(
            books = latestBooks,
            authors = authors,
            searchQuery = query,
            sortOrder = sortOrder,
            favoriteBookIds = favoriteIds,
            persistedOrderLines = cartItems,
            catalogStatus = catalog.toCatalogStatus()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = initialUiState
    )
    private val _checkoutUiState = MutableStateFlow(CheckoutUiState())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState.asStateFlow()
    private val _submitOrderUiState =
        MutableStateFlow<SubmitOrderUiState>(SubmitOrderUiState.Idle)

    val submitOrderUiState: StateFlow<SubmitOrderUiState> =
        _submitOrderUiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        if (query != searchQuery.value) {
            searchQuery.value = query
        }
    }

    fun clearSearchQuery() {
        updateSearchQuery("")
    }

    fun updateSortOrder(newSortOrder: CatalogSortOrder) {
        viewModelScope.launch {
            reportPersistenceResult(catalogPreferencesRepository.saveSortOrder(newSortOrder))
        }
    }

    fun retryCatalog() {
        catalogRetryCount.update { count -> count + 1 }
    }

    fun findBookById(bookId: String): Books? =
        uiState.value.books.firstOrNull { book -> book.id == bookId }

    fun addToOrder(bookId: String, quantity: Int = 1): OrderResult {
        val currentState = uiState.value
        val book = currentState.books.firstOrNull { it.id == bookId }
        val currentQuantity = currentState.orderLines
            .firstOrNull { it.bookId == bookId }
            ?.quantity ?: 0
        val result = validateOrderAddition(book, currentQuantity, quantity)
        if (result == OrderResult.ADDED) {
            viewModelScope.launch {
                reportPersistenceResult(cartRepository.saveItem(CartItem(bookId, currentQuantity + quantity)))
            }
        }
        return result
    }

    fun increaseQuantity(bookId: String): OrderResult {
        if (uiState.value.orderLines.none { it.bookId == bookId }) {
            return OrderResult.BOOK_NOT_FOUND
        }
        return addToOrder(bookId, quantity = 1)
    }

    fun decreaseQuantity(bookId: String) {
        val line = uiState.value.orderLines.firstOrNull { it.bookId == bookId } ?: return
        viewModelScope.launch {
            if (line.quantity <= 1) {
                reportPersistenceResult(cartRepository.removeItem(bookId))
            } else {
                reportPersistenceResult(cartRepository.saveItem(CartItem(bookId, line.quantity - 1)))
            }
        }
    }

    fun removeFromOrder(bookId: String) {
        if (uiState.value.orderLines.none { it.bookId == bookId }) {
            return
        }
        viewModelScope.launch {
            reportPersistenceResult(cartRepository.removeItem(bookId))
        }
    }

    fun toggleFavorite(bookId: String) {
        val currentState = uiState.value
        if (currentState.books.none { it.id == bookId }) {
            return
        }
        viewModelScope.launch {
            if (bookId in currentState.favoriteBookIds) {
                reportPersistenceResult(favoritesRepository.removeFavorite(bookId))
            } else {
                reportPersistenceResult(favoritesRepository.addFavorite(bookId))
            }
        }
    }

    fun updateFullName(value: String) {
        _checkoutUiState.update { currentState ->
            currentState.copy(
                fullName = value,
                isFullNameTouched = true
            )
        }
    }

    fun updatePhone(value: String) {
        _checkoutUiState.update { currentState ->
            currentState.copy(
                phone = value,
                isPhoneTouched = true
            )
        }
    }

    fun updateNit(value: String) {
        _checkoutUiState.update { currentState ->
            currentState.copy(
                nit = value,
                isNitTouched = true
            )
        }
    }

    fun updateBusinessName(value: String) {
        _checkoutUiState.update { currentState ->
            currentState.copy(
                businessName = value,
                isBusinessNameTouched = true
            )
        }
    }

    fun updateBillingType(value: BillingType) {
        _checkoutUiState.update { currentState ->
            if (value == BillingType.CF) {
                currentState.copy(
                    billingType = BillingType.CF,
                    isNitTouched = false,
                    isBusinessNameTouched = false
                )
            } else {
                currentState.copy(billingType = BillingType.NIT)
            }
        }
    }
    
    fun updatePaymentMethod(value: PaymentMethod) {
        _checkoutUiState.update { currentState -> currentState.copy(paymentMethod = value) }
    }

    @Synchronized
    fun confirmOrder() {
        if (_submitOrderUiState.value is SubmitOrderUiState.Loading) {
            return
        }

        val checkoutSnapshot = _checkoutUiState.value
        val orderSnapshot = uiState.value

        if (!checkoutSnapshot.isFormValid || orderSnapshot.orderLines.isEmpty()) {
            return
        }

        val request = OrderSubmission(
            createdAt = Instant.now().toString(),
            billingType = checkoutSnapshot.billingType.name,
            total = orderSnapshot.orderTotal,
            lines = orderSnapshot.orderLines.map { line ->
                RemoteOrderLine(
                    bookId = line.bookId,
                    title = line.title,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity,
                    subtotal = line.subtotal
                )
            },
            paymentMethod = checkoutSnapshot.paymentMethod.name
        )

        _submitOrderUiState.value = SubmitOrderUiState.Loading

        viewModelScope.launch {
            remoteOrdersRepository.createOrder(request).fold(
                onSuccess = { createdOrder ->
                    _submitOrderUiState.value = SubmitOrderUiState.Success(createdOrder)
                    reportPersistenceResult(cartRepository.clearCart())
                    _checkoutUiState.value = CheckoutUiState()
                },
                onFailure = {
                    _submitOrderUiState.value = SubmitOrderUiState.Error(
                        "No se pudo registrar el pedido. Revisa tu conexión e inténtalo nuevamente."
                    )
                }
            )
        }
    }
    fun resetSubmitOrderState() {
        if (_submitOrderUiState.value !is SubmitOrderUiState.Loading) {
            _submitOrderUiState.value = SubmitOrderUiState.Idle
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as BookStoreApplication
                val container = application.container
                StoreViewModel(
                    authRepository = container.authRepository,
                    bookRepository = container.bookRepository,
                    favoritesRepository = container.favoritesRepository,
                    cartRepository = container.cartRepository,
                    catalogPreferencesRepository = container.catalogPreferencesRepository,
                    remoteOrdersRepository = container.remoteOrdersRepository
                )
            }
        }
    }
}
