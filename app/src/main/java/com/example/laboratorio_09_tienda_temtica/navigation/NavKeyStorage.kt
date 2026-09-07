package com.example.laboratorio_09_tienda_temtica.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface StoreNavKey : NavKey
@Serializable
data object CatalogKey : StoreNavKey
@Serializable
data class BookDetailKey(
    val bookId: String
) : StoreNavKey
@Serializable
data class AuthorProfileKey(
    val authorId: String
) : StoreNavKey