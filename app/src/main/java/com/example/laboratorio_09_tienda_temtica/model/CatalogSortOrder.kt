package com.example.laboratorio_09_tienda_temtica.model

enum class CatalogSortOrder(val storedValue: String) {
    NAME("name"),
    PRICE("price");

    companion object {
        fun fromStoredValue(value: String?): CatalogSortOrder =
            entries.firstOrNull { it.storedValue == value } ?: NAME
    }
}
