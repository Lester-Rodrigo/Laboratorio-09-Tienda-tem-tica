package com.example.laboratorio_09_tienda_temtica.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

data class OrderLine(
    val bookId: String,
    val title: String,
    val unitPrice: BigDecimal,
    val quantity: Int
) {
    val subtotal: BigDecimal
        get() = unitPrice
            .multiply(BigDecimal(quantity))
            .setScale(2, RoundingMode.HALF_UP)
}

enum class OrderResult {
    ADDED,
    BOOK_NOT_FOUND,
    INVALID_QUANTITY,
    INSUFFICIENT_STOCK
}

fun Double.toMoney(): BigDecimal =
    BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP)

fun formatQuetzales(amount: BigDecimal): String =
    String.format(Locale.US, "Q %,.2f", amount.setScale(2, RoundingMode.HALF_UP))

fun formatQuetzales(amount: Double): String = formatQuetzales(amount.toMoney())
