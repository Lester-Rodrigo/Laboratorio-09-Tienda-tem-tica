package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.repository.CatalogReadError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val BOOKS_COLLECTION = "books"

class FirestoreBookDataSource(
    private val firestore: FirebaseFirestore
) : BookRemoteDataSource {

    override fun observeBookDocuments(): Flow<RemoteBookSnapshot> = callbackFlow {
        // Metadata changes are included so the switch from cache to server is also emitted.
        val registration = firestore.collection(BOOKS_COLLECTION)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(CatalogReadException(error.code.toCatalogReadError(), error))
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                trySend(
                    RemoteBookSnapshot(
                        documents = snapshot.documents.map { document ->
                            RemoteBookDocument(id = document.id, fields = document.data)
                        },
                        isFromCache = snapshot.metadata.isFromCache
                    )
                )
            }

        awaitClose {
            registration.remove()
        }
    }
}

private fun FirebaseFirestoreException.Code.toCatalogReadError(): CatalogReadError = when (this) {
    FirebaseFirestoreException.Code.UNAVAILABLE,
    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> CatalogReadError.CONNECTION
    FirebaseFirestoreException.Code.PERMISSION_DENIED,
    FirebaseFirestoreException.Code.UNAUTHENTICATED -> CatalogReadError.PERMISSION_DENIED
    else -> CatalogReadError.UNKNOWN
}
