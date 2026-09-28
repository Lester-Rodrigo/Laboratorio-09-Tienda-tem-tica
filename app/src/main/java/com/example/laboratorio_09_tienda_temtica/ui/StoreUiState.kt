package com.example.laboratorio_09_tienda_temtica.ui

import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.OrderLine
import java.math.BigDecimal

data class StoreUiState(
    val books: List<Books> = emptyList(),
    val filteredBooks: List<Books> = emptyList(),
    val authors: List<AuthorProfile> = emptyList(),
    val favoriteBookIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val orderLines: List<OrderLine> = emptyList(),
    val orderTotal: BigDecimal = BigDecimal.ZERO.setScale(2),
    val orderUnitCount: Int = 0
)
