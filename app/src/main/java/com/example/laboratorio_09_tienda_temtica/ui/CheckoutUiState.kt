package com.example.laboratorio_09_tienda_temtica.ui

import com.example.laboratorio_09_tienda_temtica.model.validateBusinessName
import com.example.laboratorio_09_tienda_temtica.model.validateFullName
import com.example.laboratorio_09_tienda_temtica.model.validateNit
import com.example.laboratorio_09_tienda_temtica.model.validatePhoneNumber

enum class BillingType { CF, NIT }
enum class PaymentMethod { CASH_ON_DELIVERY, BANK_TRANSFER }

data class CheckoutUiState(
    val fullName: String = "",
    val phone: String = "",
    val billingType: BillingType = BillingType.CF,
    val nit: String = "",
    val businessName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val isFullNameTouched: Boolean = false,
    val isPhoneTouched: Boolean = false,
    val isNitTouched: Boolean = false,
    val isBusinessNameTouched: Boolean = false
) {
    val fullNameError: String?
        get() = validateFullName(fullName)
    val phoneError: String?
        get() = validatePhoneNumber(phone)
    val nitError: String?
        get() = if (billingType == BillingType.NIT) validateNit(nit) else null
    val businessNameError: String?
        get() = if (billingType == BillingType.NIT) {
            validateBusinessName(businessName)
        } else {
            null
        }
    val visibleFullNameError: String?
        get() = fullNameError.takeIf { isFullNameTouched }
    val visiblePhoneError: String?
        get() = phoneError.takeIf { isPhoneTouched }
    val visibleNitError: String?
        get() = nitError.takeIf { isNitTouched }
    val visibleBusinessNameError: String?
        get() = businessNameError.takeIf { isBusinessNameTouched }
    val isFormValid: Boolean
        get() = fullNameError == null &&
                phoneError == null &&
                nitError == null &&
                businessNameError == null
}