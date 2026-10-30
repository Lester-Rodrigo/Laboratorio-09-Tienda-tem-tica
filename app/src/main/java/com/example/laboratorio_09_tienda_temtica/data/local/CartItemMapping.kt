package com.example.laboratorio_09_tienda_temtica.data.local

import com.example.laboratorio_09_tienda_temtica.model.CartItem

internal fun OrderLineEntity.toCartItem(): CartItem = CartItem(bookId, quantity)

internal fun CartItem.toEntity(): OrderLineEntity = OrderLineEntity(bookId, quantity)
