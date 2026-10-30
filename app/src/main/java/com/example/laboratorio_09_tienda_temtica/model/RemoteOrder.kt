package com.example.laboratorio_09_tienda_temtica.model

import java.math.BigDecimal

data class OrderSubmission(
    val createdAt: String,
    val billingType: String,
    val total: BigDecimal,
    val lines: List<RemoteOrderLine>,
    val paymentMethod: String
)

data class RemoteOrder(
    val id: String,
    val createdAt: String,
    val billingType: String,
    val total: BigDecimal,
    val lines: List<RemoteOrderLine>,
    val paymentMethod: String
)

data class RemoteOrderLine(
    val bookId: String,
    val title: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
)

enum class RemoteOrderFailure { CONNECTION, NOT_FOUND, SERVER, INVALID_DATA }

class RemoteOrderException(val reason: RemoteOrderFailure) : Exception()
