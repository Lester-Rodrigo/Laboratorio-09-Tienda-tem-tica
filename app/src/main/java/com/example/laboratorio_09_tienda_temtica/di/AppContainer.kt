package com.example.laboratorio_09_tienda_temtica.di

import com.example.laboratorio_09_tienda_temtica.data.remote.FirebaseAuthRepository
import com.example.laboratorio_09_tienda_temtica.data.remote.FirestoreBookDataSource
import com.example.laboratorio_09_tienda_temtica.data.remote.FirestoreBookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.AuthRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer {
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(FirebaseAuth.getInstance())
    }

    val bookRepository: BookRepository by lazy {
        FirestoreBookRepository(FirestoreBookDataSource(FirebaseFirestore.getInstance()))
    }
}
