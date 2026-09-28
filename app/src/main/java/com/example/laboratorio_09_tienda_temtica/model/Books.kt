package com.example.laboratorio_09_tienda_temtica.model

data class Books(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val authorId: String,
    val details: String,
    val genre: String,
    val format: String,
    val stock: Int,
    val imageUrl: String
)
