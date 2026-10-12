package com.example.laboratorio_09_tienda_temtica.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laboratorio_09_tienda_temtica.data.local.FavoriteEntity
import com.example.laboratorio_09_tienda_temtica.data.local.OrderLineEntity
import com.example.laboratorio_09_tienda_temtica.data.local.StoreDatabase
import com.example.laboratorio_09_tienda_temtica.data.preferences.StorePreferences
import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.OrderResult
import com.example.laboratorio_09_tienda_temtica.model.generateBookCatalog
import com.example.laboratorio_09_tienda_temtica.model.stableBookCoverUrl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StoreDatabase.getInstance(application)
    private val storeDao = database.storeDao()
    private val storePreferences = StorePreferences(application)
    private val originalBooks = listOf(
        Books(
            id = "book-1",
            title = "El principito",
            description = "Una fábula sobre la amistad, el amor y aquello que realmente importa.",
            price = 89.90,
            authorId = "author-1",
            details = "Antoine de Saint-Exupéry · Primera edición: 1943 · Narrativa breve",
            genre = "Narrativa breve",
            format = "Tapa blanda",
            stock = 8,
            imageUrl = stableBookCoverUrl("book-1")
        ),
        Books(
            id = "book-2",
            title = "Cien años de soledad",
            description = "La historia de la familia Buendía y el pueblo inolvidable de Macondo.",
            price = 149.50,
            authorId = "author-2",
            details = "Gabriel García Márquez · Primera edición: 1967 · Realismo mágico",
            genre = "Realismo mágico",
            format = "Tapa dura",
            stock = 3,
            imageUrl = stableBookCoverUrl("book-2")
        ),
        Books(
            id = "book-3",
            title = "El amor en los tiempos del cólera",
            description = "Una historia sobre un amor que persiste durante más de medio siglo.",
            price = 124.00,
            authorId = "author-2",
            details = "Gabriel García Márquez · Primera edición: 1985 · Novela",
            genre = "Romance",
            format = "Tapa blanda",
            stock = 0,
            imageUrl = stableBookCoverUrl("book-3")
        )
    )
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
    private val completeCatalog = generateBookCatalog(
        originalBooks = originalBooks,
        authorProfiles = authors
    )
    private val searchQuery = MutableStateFlow("")
    private val initialUiState = buildStoreUiState(
        books = completeCatalog,
        authors = authors,
        searchQuery = "",
        sortOrder = CatalogSortOrder.NAME,
        favoriteBookIds = emptyList(),
        persistedOrderLines = emptyList()
    )
    val uiState: StateFlow<StoreUiState> = combine(
        searchQuery,
        storeDao.observeFavoriteBookIds(),
        storeDao.observeOrderLines(),
        storePreferences.sortOrder
    ) { query, favoriteIds, orderLineEntities, sortOrder ->
        buildStoreUiState(
            books = completeCatalog,
            authors = authors,
            searchQuery = query,
            sortOrder = sortOrder,
            favoriteBookIds = favoriteIds,
            persistedOrderLines = orderLineEntities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialUiState
    )
    private val _checkoutUiState = MutableStateFlow(CheckoutUiState())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState.asStateFlow()
    private val _receipt = MutableStateFlow<OrderReceipt?>(null)
    val receipt: StateFlow<OrderReceipt?> = _receipt.asStateFlow()
    private var orderSequence = 0

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

    fun findBookById(bookId: String): Books? =
        completeCatalog.firstOrNull { book -> book.id == bookId }

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
    fun confirmOrder(): Boolean {
        val checkout = _checkoutUiState.value
        val order = uiState.value
        if (!checkout.isFormValid || order.orderUnitCount <= 0) {
            return false
        }
        val nextSequence = orderSequence + 1
        val confirmedReceipt = OrderReceipt(
            folio = generateOrderFolio(nextSequence),
            customerName = checkout.fullName,
            customerPhone = checkout.phone,
            billingType = checkout.billingType,
            nit = if (checkout.billingType == BillingType.NIT) checkout.nit else null,
            businessName = if (checkout.billingType == BillingType.NIT) {
                checkout.businessName
            } else {
                null
            },
            paymentMethod = checkout.paymentMethod,
            total = order.orderTotal
        )
        _receipt.value = confirmedReceipt
        viewModelScope.launch {
            storeDao.clearOrder()
        }
        _checkoutUiState.value = CheckoutUiState()
        orderSequence = nextSequence
        return true
    }
}
