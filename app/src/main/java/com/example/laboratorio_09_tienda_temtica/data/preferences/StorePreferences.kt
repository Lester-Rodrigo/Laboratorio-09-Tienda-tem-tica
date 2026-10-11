package com.example.laboratorio_09_tienda_temtica.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.CatalogSortOrder
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "store_preferences")

private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")

class StorePreferences(private val context: Context) {

    val sortOrder: Flow<CatalogSortOrder> = context.dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            CatalogSortOrder.fromStoredValue(preferences[SORT_ORDER_KEY])
        }

    suspend fun saveSortOrder(sortOrder: CatalogSortOrder) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.storedValue
        }
    }
}

fun sortBooks(books: List<Books>, sortOrder: CatalogSortOrder): List<Books> =
    when (sortOrder) {
        CatalogSortOrder.NAME -> books.sortedBy { it.title }
        CatalogSortOrder.PRICE -> books.sortedBy { it.price }
    }
