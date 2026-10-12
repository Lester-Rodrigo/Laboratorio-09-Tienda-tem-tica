package com.example.laboratorio_09_tienda_temtica.data.remote

import java.math.BigDecimal
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderDtosSerializationTest {

    @Test
    fun realGetResponseDeserializesCompleteOrderList() {
        val orders = networkJson.decodeFromString<List<OrderDto>>(REAL_GET_RESPONSE)

        assertEquals(1, orders.size)
        val order = orders.single()
        assertEquals("1", order.id)
        assertEquals("2026-10-08T18:54:00.000Z", order.createdAt)
        assertEquals("CF", order.billingType)
        assertEquals("125.50", order.total)
        assertEquals("CASH_ON_DELIVERY", order.paymentMethod)
        assertEquals(2, order.lines.size)

        val firstLine = order.lines[0]
        assertEquals("book-001", firstLine.bookId)
        assertEquals("Cien años de soledad", firstLine.title)
        assertEquals("45.00", firstLine.unitPrice)
        assertEquals(2, firstLine.quantity)
        assertEquals("90.00", firstLine.subtotal)

        val secondLine = order.lines[1]
        assertEquals("book-002", secondLine.bookId)
        assertEquals("El principito", secondLine.title)
        assertEquals("35.50", secondLine.unitPrice)
        assertEquals(1, secondLine.quantity)
        assertEquals("35.50", secondLine.subtotal)
    }

    @Test
    fun realPostResponseDeserializesTextualRemoteId() {
        val order = networkJson.decodeFromString<OrderDto>(REAL_POST_RESPONSE)

        assertEquals("1", order.id)
        assertEquals("125.50", order.total)
        assertEquals(3, order.lines.sumOf { line -> line.quantity })
    }

    @Test
    fun unknownFieldsAreIgnoredAtOrderAndLineLevels() {
        val order = networkJson.decodeFromString<OrderDto>(RESPONSE_WITH_UNKNOWN_FIELDS)

        assertEquals("1", order.id)
        assertEquals("book-001", order.lines.single().bookId)
    }

    @Test
    fun postBodySerializationOmitsRemoteIdAndPreservesContractFields() {
        val request = CreateOrderRequestDto(
            createdAt = "2026-10-08T18:54:00.000Z",
            billingType = "CF",
            total = "125.50",
            lines = listOf(
                OrderLineDto(
                    bookId = "book-001",
                    title = "Cien años de soledad",
                    unitPrice = "45.00",
                    quantity = 2,
                    subtotal = "90.00"
                ),
                OrderLineDto(
                    bookId = "book-002",
                    title = "El principito",
                    unitPrice = "35.50",
                    quantity = 1,
                    subtotal = "35.50"
                )
            ),
            paymentMethod = "CASH_ON_DELIVERY"
        )

        val body = networkJson.parseToJsonElement(
            networkJson.encodeToString(request)
        ).jsonObject

        assertFalse("id" in body)
        assertEquals("2026-10-08T18:54:00.000Z", body.getValue("createdAt").jsonPrimitive.content)
        assertEquals("CF", body.getValue("billingType").jsonPrimitive.content)
        assertEquals("125.50", body.getValue("total").jsonPrimitive.content)
        assertEquals("CASH_ON_DELIVERY", body.getValue("paymentMethod").jsonPrimitive.content)
        assertEquals(2, body.getValue("lines").jsonArray.size)
        assertEquals(
            "90.00",
            body.getValue("lines").jsonArray[0].jsonObject
                .getValue("subtotal").jsonPrimitive.content
        )
    }

    @Test
    fun decimalStringsConvertToBigDecimalWithoutPrecisionLoss() {
        val order = networkJson.decodeFromString<OrderDto>(REAL_POST_RESPONSE)

        val calculatedTotal = order.lines.fold(BigDecimal.ZERO) { total, line ->
            val unitPrice = BigDecimal(line.unitPrice)
            val calculatedSubtotal = unitPrice.multiply(BigDecimal(line.quantity))
            assertEquals(BigDecimal(line.subtotal), calculatedSubtotal)
            total.add(calculatedSubtotal)
        }

        assertEquals(BigDecimal(order.total), calculatedTotal)
        assertTrue(order.lines.all { line -> BigDecimal(line.unitPrice).scale() == 2 })
        assertTrue(order.lines.all { line -> BigDecimal(line.subtotal).scale() == 2 })
        assertEquals(2, BigDecimal(order.total).scale())
    }

    private companion object {
        val REAL_POST_RESPONSE =
            """
            {
              "createdAt": "2026-10-08T18:54:00.000Z",
              "billingType": "CF",
              "total": "125.50",
              "lines": [
                {
                  "bookId": "book-001",
                  "title": "Cien años de soledad",
                  "unitPrice": "45.00",
                  "quantity": 2,
                  "subtotal": "90.00"
                },
                {
                  "bookId": "book-002",
                  "title": "El principito",
                  "unitPrice": "35.50",
                  "quantity": 1,
                  "subtotal": "35.50"
                }
              ],
              "paymentMethod": "CASH_ON_DELIVERY",
              "id": "1"
            }
            """.trimIndent()

        val REAL_GET_RESPONSE = "[$REAL_POST_RESPONSE]"

        val RESPONSE_WITH_UNKNOWN_FIELDS =
            """
            {
              "createdAt": "2026-10-08T18:54:00.000Z",
              "billingType": "CF",
              "total": "90.00",
              "lines": [
                {
                  "bookId": "book-001",
                  "title": "Cien años de soledad",
                  "unitPrice": "45.00",
                  "quantity": 2,
                  "subtotal": "90.00",
                  "serverLineNote": "ignored"
                }
              ],
              "paymentMethod": "CASH_ON_DELIVERY",
              "id": "1",
              "serverMetadata": "ignored"
            }
            """.trimIndent()
    }
}
