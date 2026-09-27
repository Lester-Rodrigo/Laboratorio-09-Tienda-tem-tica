package com.example.laboratorio_09_tienda_temtica.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.laboratorio_09_tienda_temtica.model.formatQuetzales
import com.example.laboratorio_09_tienda_temtica.ui.BillingType
import com.example.laboratorio_09_tienda_temtica.ui.OrderReceipt
import com.example.laboratorio_09_tienda_temtica.ui.PaymentMethod

@Composable
fun ConfirmationScreen(
    receipt: OrderReceipt,
    onGoToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "¡Pedido confirmado!",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Orden registrada exitosamente en tu librería.",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        ReceiptCard(
            receipt = receipt,
            modifier = Modifier.padding(top = 24.dp)
        )

        Button(
            onClick = onGoToCatalog,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .heightIn(min = 48.dp)
        ) {
            Text(text = "Volver al catálogo")
        }
    }
}

@Composable
private fun ReceiptCard(
    receipt: OrderReceipt,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReceiptRow(
                label = "Folio",
                value = receipt.folio
            )

            ReceiptRow(
                label = "Cliente",
                value = receipt.customerName
            )

            ReceiptRow(
                label = "Facturación",
                value = billingDescription(receipt)
            )

            ReceiptRow(
                label = "Método de pago",
                value = paymentDescription(receipt.paymentMethod)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp)
            )

            ReceiptRow(
                label = "Total del pedido",
                value = formatQuetzales(receipt.total),
                emphasize = true
            )
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(1f),
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (emphasize) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
        Text(
            text = value,
            modifier = Modifier
                .weight(1.4f)
                .padding(start = 12.dp),
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (emphasize) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (emphasize) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            textAlign = TextAlign.End
        )
    }
}
private fun billingDescription(receipt: OrderReceipt): String =
    when (receipt.billingType) {
        BillingType.CF -> "CF (Consumidor Final)"

        BillingType.NIT -> buildString {
            append("NIT ${receipt.nit.orEmpty()}")

            if (!receipt.businessName.isNullOrBlank()) {
                append(" · ")
                append(receipt.businessName)
            }
        }
    }
private fun paymentDescription(
    paymentMethod: PaymentMethod
): String =
    when (paymentMethod) {
        PaymentMethod.CASH_ON_DELIVERY -> "Efectivo contra entrega"
        PaymentMethod.BANK_TRANSFER -> "Transferencia bancaria"
    }
