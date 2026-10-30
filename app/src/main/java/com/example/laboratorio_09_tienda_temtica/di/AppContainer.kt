package com.example.laboratorio_09_tienda_temtica.di

import android.content.Context
import com.example.laboratorio_09_tienda_temtica.data.local.RoomCartRepository
import com.example.laboratorio_09_tienda_temtica.data.local.RoomFavoritesRepository
import com.example.laboratorio_09_tienda_temtica.data.local.StoreDatabase
import com.example.laboratorio_09_tienda_temtica.data.preferences.DataStoreCatalogPreferencesRepository
import com.example.laboratorio_09_tienda_temtica.data.preferences.StorePreferences
import com.example.laboratorio_09_tienda_temtica.data.remote.FirebaseAuthRepository
import com.example.laboratorio_09_tienda_temtica.data.remote.FirestoreBookDataSource
import com.example.laboratorio_09_tienda_temtica.data.remote.FirestoreBookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.AuthRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CartRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogPreferencesRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext
    private val database by lazy { StoreDatabase.getInstance(applicationContext) }
    private val storeDao by lazy { database.storeDao() }
    private val preferences by lazy { StorePreferences(applicationContext) }

    val favoritesRepository: FavoritesRepository by lazy {
        RoomFavoritesRepository(storeDao)
    }

    val cartRepository: CartRepository by lazy {
        RoomCartRepository(storeDao)
    }

    val catalogPreferencesRepository: CatalogPreferencesRepository by lazy {
        DataStoreCatalogPreferencesRepository(preferences)
    }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(FirebaseAuth.getInstance())
    }

    val bookRepository: BookRepository by lazy {
        FirestoreBookRepository(FirestoreBookDataSource(FirebaseFirestore.getInstance()))
    }
}
