package com.example.laboratorio_09_tienda_temtica.data.local

import com.example.laboratorio_09_tienda_temtica.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CartItemMappingTest {
    @Test
    fun entityMapsToDomainWithoutChangingIdentityOrQuantity() {
        assertEquals(CartItem("book-2", 3), OrderLineEntity("book-2", 3).toCartItem())
    }

    @Test
    fun domainMapsBackToEntityWithoutCatalogFields() {
        val item = CartItem("generated-book-001", 2)
        assertEquals(OrderLineEntity(item.bookId, 2), item.toEntity())
        assertEquals(item, item.toEntity().toCartItem())
    }
}
