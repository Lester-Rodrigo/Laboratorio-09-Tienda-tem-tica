package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.Books
import java.math.BigDecimal

/** Maps the fields stored in `books`, ignoring any ID field inside the document. */
internal fun RemoteBookDocument.toBookOrNull(): Books? {
    val fields = fields ?: return null
    if (id.isBlank()) return null

    val title = fields.requiredText("title") ?: return null
    val description = fields.requiredText("description") ?: return null
    val authorId = fields.requiredText("authorId") ?: return null
    val details = fields.requiredText("details") ?: return null
    val genre = fields.requiredText("genre") ?: return null
    val format = fields.requiredText("format") ?: return null
    val imageUrl = fields.requiredText("imageUrl") ?: return null
    val price = parsePrice(fields["price"]) ?: return null
    val stock = parseStock(fields["stock"]) ?: return null

    return Books(
        id = id,
        title = title,
        description = description,
        price = price,
        authorId = authorId,
        details = details,
        genre = genre,
        format = format,
        stock = stock,
        imageUrl = imageUrl
    )
}

/** Accepts positive numbers (or numeric text) with at most two decimals. */
internal fun parsePrice(value: Any?): Double? {
    val amount = when (value) {
        is Long -> BigDecimal.valueOf(value)
        is Int -> BigDecimal.valueOf(value.toLong())
        is Double -> if (value.isFinite()) BigDecimal.valueOf(value) else return null
        is String -> value.trim().toBigDecimalOrNull() ?: return null
        else -> return null
    }
    if (amount.signum() <= 0) return null
    if (amount.stripTrailingZeros().scale() > 2) return null
    return amount.toDouble()
}

/** Accepts whole, non-negative units that fit in an Int; fractions and overflow are rejected. */
internal fun parseStock(value: Any?): Int? {
    val units = when (value) {
        is Long -> value
        is Int -> value.toLong()
        // Firestore may store 5 as 5.0 when edited from the console.
        is Double -> if (value.isFinite() && value % 1.0 == 0.0 && value <= Int.MAX_VALUE) {
            value.toLong()
        } else {
            return null
        }
        is String -> value.trim().toLongOrNull() ?: return null
        else -> return null
    }
    return units.takeIf { it in 0L..Int.MAX_VALUE.toLong() }?.toInt()
}

private fun Map<String, Any?>.requiredText(field: String): String? =
    (this[field] as? String)?.takeIf { it.isNotBlank() }
