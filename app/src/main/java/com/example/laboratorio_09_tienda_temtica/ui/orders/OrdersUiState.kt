package com.example.laboratorio_09_tienda_temtica.ui.orders

import com.example.laboratorio_09_tienda_temtica.data.remote.OrderDto

sealed interface OrdersUiState {
    data object Initial : OrdersUiState
    data object Loading : OrdersUiState
    data class Success(val orders: List<OrderDto>) : OrdersUiState
    data object Empty : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}
