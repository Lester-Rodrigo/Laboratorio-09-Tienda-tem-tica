package com.example.laboratorio_09_tienda_temtica.ui

import java.math.BigDecimal

data class OrderReceipt(
    val folio: String,
    val customerName: String,
    val customerPhone: String,
    val billingType: BillingType,
    val nit: String?,
    val businessName: String?,
    val paymentMethod: PaymentMethod,
    val total: BigDecimal
)

fun generateOrderFolio(sequence: Int): String {
    require(sequence > 0) {
        "La secuencia del folio debe ser mayor que cero."
    }
    return "#ORD-${sequence.toString().padStart(5, '0')}"
}
