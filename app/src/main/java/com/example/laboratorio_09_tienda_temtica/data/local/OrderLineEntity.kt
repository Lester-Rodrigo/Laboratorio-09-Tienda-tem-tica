package com.example.laboratorio_09_tienda_temtica.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "order_lines")
data class OrderLineEntity(
    @PrimaryKey
    val bookId: String,
    val quantity: Int
)
