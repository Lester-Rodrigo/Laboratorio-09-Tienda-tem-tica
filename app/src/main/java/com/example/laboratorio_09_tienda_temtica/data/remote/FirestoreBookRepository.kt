package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.repository.BookRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreBookRepository(
    private val firestore: FirebaseFirestore
) : BookRepository {

    override fun observeBooks(): Flow<List<Books>> = callbackFlow {
        val registration = firestore.collection("books").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val books = snapshot?.documents.orEmpty().mapNotNull { it.toBookOrNull() }
            trySend(books)
        }

        awaitClose {
            registration.remove()
        }
    }
}
