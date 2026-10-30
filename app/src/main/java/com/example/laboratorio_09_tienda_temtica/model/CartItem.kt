package com.example.laboratorio_09_tienda_temtica.model

/** Persisted cart identity and quantity; catalog details are resolved separately. */
data class CartItem(val bookId: String, val quantity: Int)
