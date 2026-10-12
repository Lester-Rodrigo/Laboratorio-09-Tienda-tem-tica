package com.example.laboratorio_09_tienda_temtica.ui.state

import com.example.laboratorio_09_tienda_temtica.data.remote.OrderDto

sealed interface SubmitOrderUiState {
    data object Idle : SubmitOrderUiState
    data object Loading : SubmitOrderUiState
    data class Success(val order: OrderDto) : SubmitOrderUiState
    data class Error(val message: String) : SubmitOrderUiState
}