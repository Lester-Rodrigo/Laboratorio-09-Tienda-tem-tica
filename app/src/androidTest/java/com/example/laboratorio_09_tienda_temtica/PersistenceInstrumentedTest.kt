package com.example.laboratorio_09_tienda_temtica

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.laboratorio_09_tienda_temtica.data.local.FavoriteEntity
import com.example.laboratorio_09_tienda_temtica.data.local.OrderLineEntity
import com.example.laboratorio_09_tienda_temtica.data.local.StoreDatabase
import com.example.laboratorio_09_tienda_temtica.data.preferences.StorePreferences
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceInstrumentedTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val database
        get() = StoreDatabase.getInstance(context)
    private val dao
        get() = database.storeDao()
    private val preferences
        get() = StorePreferences(context)

    @Before
    fun clearPersistedState() = runBlocking {
        dao.clearFavorites()
        dao.clearOrder()
        preferences.saveSortOrder(CatalogSortOrder.NAME)
    }

    @Test
    fun roomInsertsObservesAndDeletesFavoritesAndOrderLines() = runBlocking {
        dao.upsertFavorite(FavoriteEntity(bookId = "book-1"))
        dao.upsertOrderLine(OrderLineEntity(bookId = "book-2", quantity = 2))

        assertEquals(listOf("book-1"), dao.observeFavoriteBookIds().first())
        assertEquals(
            listOf(OrderLineEntity(bookId = "book-2", quantity = 2)),
            dao.observeOrderLines().first()
        )

        dao.deleteFavorite("book-1")
        dao.clearOrder()

        assertEquals(emptyList<String>(), dao.observeFavoriteBookIds().first())
        assertEquals(emptyList<OrderLineEntity>(), dao.observeOrderLines().first())
    }

    @Test
    fun dataStorePersistsCatalogSortOrder() = runBlocking {
        preferences.saveSortOrder(CatalogSortOrder.PRICE)

        assertEquals(CatalogSortOrder.PRICE, StorePreferences(context).sortOrder.first())
    }
}
