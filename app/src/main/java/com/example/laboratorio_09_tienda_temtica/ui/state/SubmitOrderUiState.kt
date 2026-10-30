package com.example.laboratorio_09_tienda_temtica.ui.state

import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder

sealed interface SubmitOrderUiState {
    data object Idle : SubmitOrderUiState
    data object Loading : SubmitOrderUiState
    data class Success(val order: RemoteOrder) : SubmitOrderUiState
    data class Error(val message: String) : SubmitOrderUiState
}
