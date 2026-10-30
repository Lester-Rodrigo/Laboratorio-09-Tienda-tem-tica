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
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderLine
import com.example.laboratorio_09_tienda_temtica.model.formatQuetzales
import java.math.BigDecimal

@Composable
fun ConfirmationScreen(
    order: RemoteOrder,
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
            text = "El pedido fue registrado correctamente.",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConfirmationRow(
                    label = "ID del servidor",
                    value = order.id
                )
                ConfirmationRow(
                    label = "Facturación",
                    value = order.billingType
                )
                ConfirmationRow(
                    label = "Método de pago",
                    value = order.paymentMethod
                )
                HorizontalDivider()
                Text(
                    text = "Productos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                order.lines.forEach { line ->
                    ConfirmationOrderLine(line = line)
                }
                HorizontalDivider()
                ConfirmationRow(
                    label = "Total",
                    value = formatRemotePrice(order.total),
                    emphasize = true
                )
            }
        }
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
private fun ConfirmationOrderLine(
    line: RemoteOrderLine,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = line.title,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${line.quantity} × ${formatRemotePrice(line.unitPrice)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Subtotal: ${formatRemotePrice(line.subtotal)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ConfirmationRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(1f),
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            modifier = Modifier
                .weight(1.4f)
                .padding(start = 12.dp),
            color = if (emphasize) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End
        )
    }
}

private fun formatRemotePrice(value: BigDecimal): String = formatQuetzales(value)
