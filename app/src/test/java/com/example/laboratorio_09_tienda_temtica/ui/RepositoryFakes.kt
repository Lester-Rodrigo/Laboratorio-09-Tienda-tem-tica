package com.example.laboratorio_09_tienda_temtica.ui

import com.example.laboratorio_09_tienda_temtica.model.CartItem
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import com.example.laboratorio_09_tienda_temtica.model.OrderSubmission
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderException
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderFailure
import com.example.laboratorio_09_tienda_temtica.model.repository.AuthRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CartRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogPreferencesRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogResult
import com.example.laboratorio_09_tienda_temtica.model.repository.FavoritesRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.RemoteOrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class FakeAuthRepository : AuthRepository {
    var calls = 0
    var result = Result.success(Unit)
    override suspend fun ensureAnonymousSession(): Result<Unit> { calls++; return result }
}

internal class FakeBookRepository : BookRepository {
    val results = MutableSharedFlow<CatalogResult>(replay = 1)
    var active = 0
    var maximumActive = 0
    var subscriptions = 0
    override fun observeCatalog() = flow {
        active++; subscriptions++; maximumActive = maxOf(maximumActive, active)
        try { emitAll(results) } finally { active-- }
    }
}

internal class FakeFavoritesRepository : FavoritesRepository {
    val ids = MutableStateFlow<List<String>>(emptyList())
    override fun observeFavoriteBookIds() = ids
    override suspend fun addFavorite(bookId: String): Result<Unit> {
        ids.value = (ids.value + bookId).distinct(); return Result.success(Unit)
    }
    override suspend fun removeFavorite(bookId: String): Result<Unit> {
        ids.value -= bookId; return Result.success(Unit)
    }
}

internal class FakeCartRepository : CartRepository {
    val items = MutableStateFlow<List<CartItem>>(emptyList())
    override fun observeItems() = items
    override suspend fun saveItem(item: CartItem): Result<Unit> {
        items.value = items.value.filterNot { it.bookId == item.bookId } + item
        return Result.success(Unit)
    }
    override suspend fun removeItem(bookId: String): Result<Unit> {
        items.value = items.value.filterNot { it.bookId == bookId }; return Result.success(Unit)
    }
    override suspend fun clearCart(): Result<Unit> { items.value = emptyList(); return Result.success(Unit) }
}

internal class FakeCatalogPreferencesRepository : CatalogPreferencesRepository {
    val order = MutableStateFlow(CatalogSortOrder.NAME)
    override fun observeSortOrder() = order
    override suspend fun saveSortOrder(sortOrder: CatalogSortOrder): Result<Unit> {
        order.value = sortOrder; return Result.success(Unit)
    }
}

internal class FakeRemoteOrdersRepository : RemoteOrdersRepository {
    var orders = emptyList<RemoteOrder>()
    var failCreate = false
    var submitted: OrderSubmission? = null
    override suspend fun createOrder(order: OrderSubmission): Result<RemoteOrder> {
        submitted = order
        return if (failCreate) Result.failure(RemoteOrderException(RemoteOrderFailure.CONNECTION))
        else Result.success(RemoteOrder("1", order.createdAt, order.billingType, order.total, order.lines, order.paymentMethod))
    }
    override suspend fun getOrders() = Result.success(orders)
    override suspend fun getOrder(id: String): Result<RemoteOrder> = orders.find { it.id == id }
        ?.let { Result.success(it) } ?: Result.failure(RemoteOrderException(RemoteOrderFailure.NOT_FOUND))
}
