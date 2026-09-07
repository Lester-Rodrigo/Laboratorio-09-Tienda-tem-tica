package com.example.laboratorio_09_tienda_temtica.ui

import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books

data class StoreUiState(
    val books: List<Books> = emptyList(),
    val authors: List<AuthorProfile> = emptyList(),
    val favoriteBookIds: Set<String> = emptySet()
)
