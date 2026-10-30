package com.example.laboratorio_09_tienda_temtica.model

fun sortBooks(books: List<Books>, sortOrder: CatalogSortOrder): List<Books> =
    when (sortOrder) {
        CatalogSortOrder.NAME -> books.sortedBy { it.title }
        CatalogSortOrder.PRICE -> books.sortedBy { it.price }
    }
