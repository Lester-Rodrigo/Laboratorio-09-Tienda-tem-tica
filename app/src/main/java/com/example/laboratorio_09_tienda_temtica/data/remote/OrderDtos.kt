package com.example.laboratorio_09_tienda_temtica.data.remote

import kotlinx.serialization.Serializable

/**
 * Body sent when a new order is created.
 *
 * The caller must build this DTO from an immutable snapshot of the current order before
 * clearing it. Personal and financial customer data must not be included.
 */
@Serializable
data class CreateOrderRequestDto(
    val createdAt: String,
    val billingType: String,
    val total: String,
    val lines: List<OrderLineDto>,
    val paymentMethod: String
)

/** Complete order returned by MockAPI, including its server-assigned textual ID. */
@Serializable
data class OrderDto(
    val createdAt: String,
    val billingType: String,
    val total: String,
    val lines: List<OrderLineDto>,
    val paymentMethod: String,
    val id: String
)

/** Historical snapshot of one purchased book. */
@Serializable
data class OrderLineDto(
    val bookId: String,
    val title: String,
    val unitPrice: String,
    val quantity: Int,
    val subtotal: String
)
