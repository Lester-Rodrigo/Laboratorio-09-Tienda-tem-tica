package com.example.laboratorio_09_tienda_temtica.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.laboratorio_09_tienda_temtica.BookStoreApplication
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderException
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderFailure
import com.example.laboratorio_09_tienda_temtica.model.repository.RemoteOrdersRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(private val repository: RemoteOrdersRepository) : ViewModel() {
    private var listJob: Job? = null
    private var detailJob: Job? = null
    private val _ordersUiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Initial)
    val ordersUiState: StateFlow<OrdersUiState> = _ordersUiState.asStateFlow()
    private val _orderDetailUiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val orderDetailUiState: StateFlow<OrderDetailUiState> = _orderDetailUiState.asStateFlow()

    fun loadOrders() {
        listJob?.cancel()
        _ordersUiState.value = OrdersUiState.Loading
        listJob = viewModelScope.launch {
            _ordersUiState.value = repository.getOrders().fold(
                onSuccess = { if (it.isEmpty()) OrdersUiState.Empty else OrdersUiState.Success(it) },
                onFailure = { OrdersUiState.Error("No se pudieron cargar los pedidos. Inténtalo nuevamente.") }
            )
        }
    }

    fun loadOrderDetail(orderId: String) {
        detailJob?.cancel()
        _orderDetailUiState.value = OrderDetailUiState.Loading
        detailJob = viewModelScope.launch {
            _orderDetailUiState.value = repository.getOrder(orderId).fold(
                onSuccess = { OrderDetailUiState.Success(it) },
                onFailure = {
                    if (it is RemoteOrderException && it.reason == RemoteOrderFailure.NOT_FOUND) {
                        OrderDetailUiState.NotFound
                    } else {
                        OrderDetailUiState.Error("No se pudo cargar el pedido. Inténtalo nuevamente.")
                    }
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as BookStoreApplication
                OrdersViewModel(application.container.remoteOrdersRepository)
            }
        }
    }
}
