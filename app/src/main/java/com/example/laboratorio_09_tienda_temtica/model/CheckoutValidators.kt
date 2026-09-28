package com.example.laboratorio_09_tienda_temtica.model

private const val MINIMUM_NAME_LETTERS = 3
private const val PHONE_DIGITS = 8
private const val MINIMUM_NIT_DIGITS = 5
private const val MINIMUM_BUSINESS_NAME_CHARACTERS = 3

fun validateFullName(value: String): String? {
    val trimmedValue = value.trim()
    if (trimmedValue.any(Char::isDigit)) {
        return "El nombre no puede contener números."
    }
    if (trimmedValue.count(Char::isLetter) < MINIMUM_NAME_LETTERS) {
        return "Ingresa un nombre con al menos 3 letras."
    }
    return null
}

fun validatePhoneNumber(value: String): String? =
    if (value.length == PHONE_DIGITS && value.all { character -> character in '0'..'9' }) {
        null
    } else {
        "Ingresa exactamente 8 dígitos."
    }

fun validateNit(value: String): String? =
    if (
        value.length >= MINIMUM_NIT_DIGITS &&
        value.all { character -> character in '0'..'9' }
    ) {
        null
    } else {
        "Ingresa al menos 5 dígitos."
    }

fun validateBusinessName(value: String): String? =
    if (value.trim().length >= MINIMUM_BUSINESS_NAME_CHARACTERS) {
        null
    } else {
        "Ingresa una razón social de al menos 3 caracteres."
    }
