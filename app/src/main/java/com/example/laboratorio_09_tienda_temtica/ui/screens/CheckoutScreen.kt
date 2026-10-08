package com.example.laboratorio_09_tienda_temtica.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import com.example.laboratorio_09_tienda_temtica.model.formatQuetzales
import com.example.laboratorio_09_tienda_temtica.ui.BillingType
import com.example.laboratorio_09_tienda_temtica.ui.CheckoutUiState
import com.example.laboratorio_09_tienda_temtica.ui.PaymentMethod
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    uiState: CheckoutUiState,
    orderTotal: BigDecimal,
    orderUnitCount: Int,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirmOrder: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val nitFocusRequester = remember { FocusRequester() }
    val currentUiState by rememberUpdatedState(uiState)
    val currentOrderUnitCount by rememberUpdatedState(orderUnitCount)
    val isConfirmEnabled by remember {
        derivedStateOf {
            currentUiState.isFormValid && currentOrderUnitCount > 0
        }
    }

    fun clearFocusAndKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al pedido"
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Total: ${formatQuetzales(orderTotal)}",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OrderCheckoutSummary(
                orderTotal = orderTotal,
                orderUnitCount = orderUnitCount
            )
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Nombre completo *") },
                placeholder = { Text(text = "Ej. María Morales") },
                singleLine = true,
                isError = uiState.visibleFullNameError != null,
                supportingText = {
                    uiState.visibleFullNameError?.let { error ->
                        Text(text = error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                )
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Teléfono / WhatsApp *") },
                placeholder = { Text(text = "Ej. 55123456") },
                singleLine = true,
                isError = uiState.visiblePhoneError != null,
                supportingText = {
                    uiState.visiblePhoneError?.let { error ->
                        Text(text = error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                    imeAction = if (uiState.billingType == BillingType.NIT) {
                        ImeAction.Next
                    } else {
                        ImeAction.Done
                    }
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (uiState.billingType == BillingType.NIT) {
                            nitFocusRequester.requestFocus()
                        }
                    },
                    onDone = {
                        clearFocusAndKeyboard()
                    }
                )
            )
            Text(
                text = "Facturación *",
                style = MaterialTheme.typography.titleMedium
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                RadioChoice(
                    text = "Consumidor Final (CF)",
                    selected = uiState.billingType == BillingType.CF,
                    onClick = {
                        clearFocusAndKeyboard()
                        onBillingTypeChange(BillingType.CF)
                    }
                )
                RadioChoice(
                    text = "Factura con NIT",
                    selected = uiState.billingType == BillingType.NIT,
                    onClick = {
                        clearFocusAndKeyboard()
                        onBillingTypeChange(BillingType.NIT)
                    }
                )
            }
            AnimatedVisibility(
                visible = uiState.billingType == BillingType.NIT
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Datos de facturación fiscal",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = uiState.nit,
                            onValueChange = onNitChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(nitFocusRequester),
                            label = { Text(text = "NIT *") },
                            singleLine = true,
                            isError = uiState.visibleNitError != null,
                            supportingText = {
                                uiState.visibleNitError?.let { error ->
                                    Text(text = error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType =
                                    androidx.compose.ui.text.input.KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )
                        )
                        OutlinedTextField(
                            value = uiState.businessName,
                            onValueChange = onBusinessNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(text = "Razón social / Nombre fiscal *")
                            },
                            placeholder = {
                                Text(text = "Ej. Librería Central S.A.")
                            },
                            singleLine = true,
                            isError =
                                uiState.visibleBusinessNameError != null,
                            supportingText = {
                                uiState.visibleBusinessNameError?.let { error ->
                                    Text(text = error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization =
                                    KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    clearFocusAndKeyboard()
                                }
                            )
                        )
                    }
                }
            }
            Text(
                text = "Método de pago *",
                style = MaterialTheme.typography.titleMedium
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                RadioChoice(
                    text = "Efectivo contra entrega",
                    selected = uiState.paymentMethod ==
                            PaymentMethod.CASH_ON_DELIVERY,
                    onClick = {
                        clearFocusAndKeyboard()
                        onPaymentMethodChange(
                            PaymentMethod.CASH_ON_DELIVERY
                        )
                    }
                )
                RadioChoice(
                    text = "Transferencia bancaria",
                    selected = uiState.paymentMethod ==
                            PaymentMethod.BANK_TRANSFER,
                    onClick = {
                        clearFocusAndKeyboard()
                        onPaymentMethodChange(
                            PaymentMethod.BANK_TRANSFER
                        )
                    }
                )
            }
            Button(
                onClick = onConfirmOrder,
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Confirmar pedido " + "(${formatQuetzales(orderTotal)})"
                )
            }

            if (!isConfirmEnabled) {
                Text(
                    text = if (orderUnitCount == 0) {
                        "El pedido no contiene unidades."
                    } else {
                        "Completa correctamente los campos obligatorios."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RadioChoice(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun OrderCheckoutSummary(
    orderTotal: BigDecimal,
    orderUnitCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Resumen del pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (orderUnitCount == 1) {
                    "1 unidad"
                } else {
                    "$orderUnitCount unidades"
                }
            )
            Text(
                text = "Total: ${formatQuetzales(orderTotal)}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
