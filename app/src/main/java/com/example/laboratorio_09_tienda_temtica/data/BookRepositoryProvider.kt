package com.example.laboratorio_09_tienda_temtica.data

import com.example.laboratorio_09_tienda_temtica.data.remote.FirestoreBookDataSource
import com.example.laboratorio_09_tienda_temtica.data.remote.FirestoreBookRepository
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.google.firebase.firestore.FirebaseFirestore

/** Single place where Firebase is wired, so the UI layer only knows [BookRepository]. */
object BookRepositoryProvider {
    val bookRepository: BookRepository by lazy {
        FirestoreBookRepository(FirestoreBookDataSource(FirebaseFirestore.getInstance()))
    }
}
