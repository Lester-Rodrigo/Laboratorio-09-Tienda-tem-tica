package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.OrderSubmission
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderLine
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderException
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderFailure
import java.math.BigDecimal
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteOrdersRepositoryTest {
    private val dto = OrderDto("2026-10-29T12:00:00Z", "CF", "125.50", listOf(
        OrderLineDto("book-1", "Book", "62.75", 2, "125.50")
    ), "CASH_ON_DELIVERY", "001")

    @Test fun responseMapperPreservesAllHistoricalFields() {
        val order = dto.toDomain()
        assertEquals(dto.id, order.id); assertEquals(dto.createdAt, order.createdAt)
        assertEquals(dto.billingType, order.billingType); assertEquals(dto.paymentMethod, order.paymentMethod)
        assertEquals(BigDecimal("125.50"), order.total)
        val line = order.lines.single()
        assertEquals("book-1", line.bookId); assertEquals("Book", line.title)
        assertEquals(2, line.quantity); assertEquals(BigDecimal("62.75"), line.unitPrice)
        assertEquals(BigDecimal("125.50"), line.subtotal)
    }

    @Test fun requestMapperUsesFixedDecimalsAndNoRemoteId() {
        val input = OrderSubmission(dto.createdAt, "CF", BigDecimal("125.5"), listOf(
            RemoteOrderLine("book-1", "Book", BigDecimal("62.75"), 2, BigDecimal("125.5"))
        ), "CASH_ON_DELIVERY")
        assertEquals(dto.copy(id = "001").lines, input.toRequestDto().lines)
        assertEquals("125.50", input.toRequestDto().total)
    }

    @Test fun requestsUseExistingApiAndDescendingDateSort() = runBlocking {
        val api = FakeOrderApi(dto)
        val repository = RetrofitRemoteOrdersRepository(api)
        assertEquals("001", repository.getOrders().getOrThrow().single().id)
        assertEquals("createdAt", api.sortBy); assertEquals("desc", api.order)
        assertEquals("001", repository.getOrder("001").getOrThrow().id)
        assertEquals("001", api.requestedId)
        val model = dto.toDomain()
        repository.createOrder(OrderSubmission(model.createdAt, model.billingType, model.total, model.lines, model.paymentMethod)).getOrThrow()
        assertEquals("125.50", api.submission?.total)
    }

    @Test fun networkErrorsBecomeDomainFailures() = runBlocking {
        val api = FakeOrderApi(dto); api.error = IOException("network")
        val error = RetrofitRemoteOrdersRepository(api).getOrders().exceptionOrNull()
        assertTrue(error is RemoteOrderException)
        assertEquals(RemoteOrderFailure.CONNECTION, (error as RemoteOrderException).reason)
    }

    @Test fun malformedMoneyBecomesInvalidData() = runBlocking {
        val result = RetrofitRemoteOrdersRepository(FakeOrderApi(dto.copy(total = "bad"))).getOrder("001")
        assertEquals(RemoteOrderFailure.INVALID_DATA, (result.exceptionOrNull() as RemoteOrderException).reason)
    }

    @Test(expected = CancellationException::class)
    fun cancellationPropagates() = runBlocking {
        val api = FakeOrderApi(dto); api.error = CancellationException("cancel")
        RetrofitRemoteOrdersRepository(api).getOrders()
        Unit
    }
}

private class FakeOrderApi(private val response: OrderDto) : OrderApi {
    var sortBy = ""
    var order = ""
    var requestedId = ""
    var submission: CreateOrderRequestDto? = null
    var error: Exception? = null
    private fun answer(): OrderDto { error?.let { throw it }; return response }
    override suspend fun createOrder(order: CreateOrderRequestDto): OrderDto { submission = order; return answer() }
    override suspend fun getOrders(sortBy: String, order: String): List<OrderDto> {
        this.sortBy = sortBy; this.order = order; return listOf(answer())
    }
    override suspend fun getOrder(id: String): OrderDto { requestedId = id; return answer() }
}
