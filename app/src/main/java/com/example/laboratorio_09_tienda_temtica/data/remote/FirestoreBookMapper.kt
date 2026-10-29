package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.Books
import com.google.firebase.firestore.DocumentSnapshot

/** Maps the fields stored in `books`, ignoring any ID field inside the document. */
internal fun DocumentSnapshot.toBookOrNull(): Books? {
    val fields = data ?: return null
    if (id.isBlank()) return null

    val title = fields.requiredText("title") ?: return null
    val description = fields.requiredText("description") ?: return null
    val authorId = fields.requiredText("authorId") ?: return null
    val details = fields.requiredText("details") ?: return null
    val genre = fields.requiredText("genre") ?: return null
    val format = fields.requiredText("format") ?: return null
    val imageUrl = fields.requiredText("imageUrl") ?: return null

    val price = when (val value = fields["price"]) {
        is Double -> value
        is Long -> value.toDouble()
        else -> return null
    }
    if (!price.isFinite() || price <= 0.0) return null

    // Firestore integers are Long; reject fractions and overflow before converting.
    val stock = fields["stock"] as? Long ?: return null
    if (stock !in 0L..Int.MAX_VALUE.toLong()) return null

    return Books(
        id = id,
        title = title,
        description = description,
        price = price,
        authorId = authorId,
        details = details,
        genre = genre,
        format = format,
        stock = stock.toInt(),
        imageUrl = imageUrl
    )
}

private fun Map<String, Any>.requiredText(field: String): String? =
    (this[field] as? String)?.takeIf { it.isNotBlank() }
