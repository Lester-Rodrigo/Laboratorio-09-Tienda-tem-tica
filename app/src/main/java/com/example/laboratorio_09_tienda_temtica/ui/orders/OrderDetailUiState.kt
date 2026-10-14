package com.example.laboratorio_09_tienda_temtica.ui.orders

import com.example.laboratorio_09_tienda_temtica.data.remote.OrderDto

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Success(val order: OrderDto) : OrderDetailUiState
    data object NotFound : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}
