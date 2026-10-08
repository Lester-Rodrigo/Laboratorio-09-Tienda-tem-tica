package com.example.laboratorio_09_tienda_temtica.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.laboratorio_09_tienda_temtica.model.Books
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "store_preferences")

const val SORT_BY_NAME = "name"
const val SORT_BY_PRICE = "price"
const val DEFAULT_SORT_ORDER = SORT_BY_NAME

private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")

class StorePreferences(private val context: Context) {

    val sortOrder: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SORT_ORDER_KEY] ?: DEFAULT_SORT_ORDER
    }

    suspend fun saveSortOrder(sortOrder: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder
        }
    }
}

fun sortBooks(books: List<Books>, sortOrder: String): List<Books> =
    when (sortOrder) {
        SORT_BY_PRICE -> books.sortedBy { it.price }
        else -> books.sortedBy { it.title }
    }
