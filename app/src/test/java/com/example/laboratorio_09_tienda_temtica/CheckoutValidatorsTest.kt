package com.example.laboratorio_09_tienda_temtica

import com.example.laboratorio_09_tienda_temtica.model.validateBusinessName
import com.example.laboratorio_09_tienda_temtica.model.validateFullName
import com.example.laboratorio_09_tienda_temtica.model.validateNit
import com.example.laboratorio_09_tienda_temtica.model.validatePhoneNumber
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull


class CheckoutValidatorsTest {
    @Test
    fun fullName_acceptsAccentsAndEnye() {
        assertNull(validateFullName("Íñigo"))
        assertNull(validateFullName("María José"))
    }

    @Test
    fun fullName_rejectsFewerThanThreeLetters() {
        assertEquals("Ingresa un nombre con al menos 3 letras.", validateFullName("A- b"))
    }

    @Test
    fun fullName_rejectsDigits() {
        assertEquals("El nombre no puede contener números.", validateFullName("Ana 2"))
    }

    @Test
    fun phone_acceptsExactlyEightDigits() {
        assertNull(validatePhoneNumber("55442211"))
    }

    @Test
    fun phone_rejectsInvalidFormats() {
        assertEquals("Ingresa exactamente 8 dígitos.", validatePhoneNumber("5544221"))
        assertEquals("Ingresa exactamente 8 dígitos.", validatePhoneNumber("554422110"))
        assertEquals("Ingresa exactamente 8 dígitos.", validatePhoneNumber("5544 2211"))
        assertEquals("Ingresa exactamente 8 dígitos.", validatePhoneNumber("5544-2211"))
    }

    @Test
    fun nit_acceptsFiveOrMoreDigits() {
        assertNull(validateNit("12345"))
        assertNull(validateNit("123456789"))
    }

    @Test
    fun nit_rejectsShortOrNonNumericValues() {
        assertEquals("Ingresa al menos 5 dígitos.", validateNit("1234"))
        assertEquals("Ingresa al menos 5 dígitos.", validateNit("1234A"))
    }

    @Test
    fun businessName_trimsBeforeValidating() {
        assertNull(validateBusinessName("  Editorial Luna  "))
        assertEquals(
            "Ingresa una razón social de al menos 3 caracteres.",
            validateBusinessName("   ")
        )
        assertEquals(
            "Ingresa una razón social de al menos 3 caracteres.",
            validateBusinessName(" ab ")
        )
    }
}