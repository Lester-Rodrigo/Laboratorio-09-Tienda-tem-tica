package com.example.laboratorio_09_tienda_temtica.model.repository

import com.example.laboratorio_09_tienda_temtica.model.Books
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun observeBooks(): Flow<List<Books>>
}
