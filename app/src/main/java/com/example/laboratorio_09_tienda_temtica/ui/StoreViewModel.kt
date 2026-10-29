package com.example.laboratorio_09_tienda_temtica.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laboratorio_09_tienda_temtica.data.BookRepositoryProvider
import com.example.laboratorio_09_tienda_temtica.data.local.FavoriteEntity
import com.example.laboratorio_09_tienda_temtica.data.local.OrderLineEntity
import com.example.laboratorio_09_tienda_temtica.data.local.StoreDatabase
import com.example.laboratorio_09_tienda_temtica.data.preferences.StorePreferences
import com.example.laboratorio_09_tienda_temtica.data.remote.CreateOrderRequestDto
import com.example.laboratorio_09_tienda_temtica.data.remote.OrderLineDto
import com.example.laboratorio_09_tienda_temtica.data.remote.OrderRemoteClient
import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.OrderResult
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogResult
import com.example.laboratorio_09_tienda_temtica.ui.state.SubmitOrderUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant

class StoreViewModel(
    application: Application,
    private val bookRepository: BookRepository
) : AndroidViewModel(application) {
    // Used by the default ViewModel factory; Firebase is wired in the data layer.
    constructor(application: Application) : this(application, BookRepositoryProvider.bookRepository)

    private val database = StoreDatabase.getInstance(application)
    private val storeDao = database.storeDao()
    private val storePreferences = StorePreferences(application)
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
    private val catalogRetryCount = MutableStateFlow(0)

    // Each retry starts a new subscription; null marks the loading state until the first result.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val catalogResult: Flow<CatalogResult?> = catalogRetryCount.flatMapLatest {
        bookRepository.observeCatalog().onStart<CatalogResult?> { emit(null) }
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
        storeDao.observeFavoriteBookIds(),
        storeDao.observeOrderLines(),
        storePreferences.sortOrder
    ) { query, catalog, favoriteIds, orderLineEntities, sortOrder ->
        buildStoreUiState(
            books = catalog.availableBooks(),
            authors = authors,
            searchQuery = query,
            sortOrder = sortOrder,
            favoriteBookIds = favoriteIds,
            persistedOrderLines = orderLineEntities,
            catalogStatus = catalog.toCatalogStatus()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
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
            storePreferences.saveSortOrder(newSortOrder)
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
                storeDao.upsertOrderLine(
                    OrderLineEntity(bookId = bookId, quantity = currentQuantity + quantity)
                )
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
                storeDao.deleteOrderLine(bookId)
            } else {
                storeDao.upsertOrderLine(
                    OrderLineEntity(bookId = bookId, quantity = line.quantity - 1)
                )
            }
        }
    }

    fun removeFromOrder(bookId: String) {
        if (uiState.value.orderLines.none { it.bookId == bookId }) {
            return
        }
        viewModelScope.launch {
            storeDao.deleteOrderLine(bookId)
        }
    }

    fun toggleFavorite(bookId: String) {
        val currentState = uiState.value
        if (currentState.books.none { it.id == bookId }) {
            return
        }
        viewModelScope.launch {
            if (bookId in currentState.favoriteBookIds) {
                storeDao.deleteFavorite(bookId)
            } else {
                storeDao.upsertFavorite(FavoriteEntity(bookId = bookId))
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

        val request = CreateOrderRequestDto(
            createdAt = Instant.now().toString(),
            billingType = checkoutSnapshot.billingType.name,
            total = orderSnapshot.orderTotal.toPlainString(),
            lines = orderSnapshot.orderLines.map { line ->
                OrderLineDto(
                    bookId = line.bookId,
                    title = line.title,
                    unitPrice = line.unitPrice.toPlainString(),
                    quantity = line.quantity,
                    subtotal = line.subtotal.toPlainString()
                )
            },
            paymentMethod = checkoutSnapshot.paymentMethod.name
        )

        _submitOrderUiState.value = SubmitOrderUiState.Loading

        viewModelScope.launch {
            try {
                val createdOrder =
                    OrderRemoteClient.orderApi.createOrder(request)

                _submitOrderUiState.value =
                    SubmitOrderUiState.Success(createdOrder)

                storeDao.clearOrder()
                _checkoutUiState.value = CheckoutUiState()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                _submitOrderUiState.value = SubmitOrderUiState.Error(
                    message = "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo nuevamente."
                )
            } catch (exception: HttpException) {
                _submitOrderUiState.value = SubmitOrderUiState.Error(
                    message = "El servidor no pudo registrar el pedido. Inténtalo nuevamente."
                )
            } catch (exception: SerializationException) {
                _submitOrderUiState.value = SubmitOrderUiState.Error(
                    message = "La respuesta del servidor no pudo procesarse."
                )
            }
        }
    }
    fun resetSubmitOrderState() {
        if (_submitOrderUiState.value !is SubmitOrderUiState.Loading) {
            _submitOrderUiState.value = SubmitOrderUiState.Idle
        }
    }
}
