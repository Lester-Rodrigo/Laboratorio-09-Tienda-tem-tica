package com.example.laboratorio_09_tienda_temtica.data.remote

internal fun validBookFields(
    title: String = "El principito",
    price: Any? = 89.90,
    stock: Any? = 8L
): Map<String, Any?> = mapOf(
    "title" to title,
    "description" to "Una fábula sobre la amistad.",
    "price" to price,
    "authorId" to "author-1",
    "details" to "Antoine de Saint-Exupéry · 1943",
    "genre" to "Narrativa breve",
    "format" to "Tapa blanda",
    "stock" to stock,
    "imageUrl" to "https://example.com/book-1.jpg"
)

internal fun bookDocument(
    id: String,
    fields: Map<String, Any?>? = validBookFields()
) = RemoteBookDocument(id = id, fields = fields)
