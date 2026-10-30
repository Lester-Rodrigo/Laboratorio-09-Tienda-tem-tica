package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.OrderSubmission
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderLine
import java.math.BigDecimal
import java.math.RoundingMode

private fun BigDecimal.fixedDecimal(): String = setScale(2, RoundingMode.UNNECESSARY).toPlainString()

internal fun OrderSubmission.toRequestDto(): CreateOrderRequestDto = CreateOrderRequestDto(
    createdAt = createdAt,
    billingType = billingType,
    total = total.fixedDecimal(),
    lines = lines.map { line ->
        OrderLineDto(line.bookId, line.title, line.unitPrice.fixedDecimal(), line.quantity, line.subtotal.fixedDecimal())
    },
    paymentMethod = paymentMethod
)

internal fun OrderLineDto.toDomain(): RemoteOrderLine = RemoteOrderLine(
    bookId, title, unitPrice.toBigDecimal(), quantity, subtotal.toBigDecimal()
)

internal fun OrderDto.toDomain(): RemoteOrder = RemoteOrder(
    id, createdAt, billingType, total.toBigDecimal(), lines.map { it.toDomain() }, paymentMethod
)
