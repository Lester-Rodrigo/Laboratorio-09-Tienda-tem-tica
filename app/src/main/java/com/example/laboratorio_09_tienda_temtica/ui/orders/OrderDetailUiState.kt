package com.example.laboratorio_09_tienda_temtica.ui.orders

import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Success(val order: RemoteOrder) : OrderDetailUiState
    data object NotFound : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}
