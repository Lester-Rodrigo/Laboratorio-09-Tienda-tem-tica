package com.example.laboratorio_09_tienda_temtica.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.laboratorio_09_tienda_temtica.data.remote.OrderDto
import com.example.laboratorio_09_tienda_temtica.data.remote.OrderLineDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    uiState: OrderDetailUiState,
    onLoadOrder: (String) -> Unit,
    onBackToOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalle del pedido") }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onBackToOrders,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Volver a Mis pedidos")
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (uiState) {
            OrderDetailUiState.Loading -> OrdersLoading(
                text = "Cargando pedido...",
                modifier = contentModifier
            )
            OrderDetailUiState.NotFound -> OrdersMessage(
                title = "Pedido no encontrado",
                message = "El pedido #$orderId no existe o fue eliminado.",
                modifier = contentModifier
            )
            is OrderDetailUiState.Error -> OrdersMessage(
                title = "No se pudo cargar el pedido",
                message = uiState.message,
                actionText = "Reintentar",
                onAction = { onLoadOrder(orderId) },
                modifier = contentModifier
            )
            is OrderDetailUiState.Success -> OrderDetailContent(
                order = uiState.order,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: OrderDto,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Pedido #${order.id}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fecha: ${order.createdAt}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        items(items = order.lines) { line ->
            OrderDetailLineCard(line = line)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.total,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun OrderDetailLineCard(
    line: OrderLineDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = line.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cantidad: ${line.quantity}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Precio unitario: ${line.unitPrice}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Subtotal: ${line.subtotal}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
