package com.example.laboratorio_09_tienda_temtica.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreBookMapperTest {

    @Test
    fun validDocumentUsesDocumentIdAndConvertsFields() {
        val fields = validBookFields() + ("id" to "id-dentro-del-documento")

        val book = bookDocument(id = "book-1", fields = fields).toBookOrNull()

        assertNotNull(book)
        assertEquals("book-1", book!!.id)
        assertEquals(89.90, book.price, 0.0)
        assertEquals(8, book.stock)
    }

    @Test
    fun priceAcceptsWholeNumbersDecimalsAndNumericText() {
        assertEquals(120.0, parsePrice(120L)!!, 0.0)
        assertEquals(120.0, parsePrice(120)!!, 0.0)
        assertEquals(89.9, parsePrice(89.9)!!, 0.0)
        assertEquals(45.5, parsePrice(" 45.50 ")!!, 0.0)
    }

    @Test
    fun priceRejectsUnsafeValues() {
        listOf(
            null, 0L, -10.0, Double.NaN, Double.POSITIVE_INFINITY,
            10.999, "abc", "", true
        ).forEach { value ->
            assertNull("price=$value", parsePrice(value))
        }
    }

    @Test
    fun stockAcceptsWholeUnits() {
        assertEquals(0, parseStock(0L))
        assertEquals(5, parseStock(5))
        assertEquals(5, parseStock(5.0))
        assertEquals(12, parseStock("12"))
    }

    @Test
    fun stockRejectsUnsafeValues() {
        listOf(
            null, -1L, 2.5, Double.NaN, Int.MAX_VALUE.toLong() + 1,
            Long.MAX_VALUE, 1e20, "3.5", "muchos", false
        ).forEach { value ->
            assertNull("stock=$value", parseStock(value))
        }
    }

    @Test
    fun documentWithMissingOrBlankFieldsIsRejected() {
        assertNull(bookDocument("book-1", fields = null).toBookOrNull())
        assertNull(bookDocument("book-1", validBookFields(title = " ")).toBookOrNull())
        assertNull(bookDocument("book-1", validBookFields() - "imageUrl").toBookOrNull())
        assertNull(bookDocument("book-1", validBookFields(price = "gratis")).toBookOrNull())
        assertNull(bookDocument("book-1", validBookFields(stock = -3L)).toBookOrNull())
        assertNull(bookDocument(" ").toBookOrNull())
    }
}
