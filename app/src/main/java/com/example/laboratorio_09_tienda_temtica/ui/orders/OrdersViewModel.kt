package com.example.laboratorio_09_tienda_temtica.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laboratorio_09_tienda_temtica.data.remote.OrderRemoteClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

private const val SORT_BY_CREATED_AT = "createdAt"
private const val ORDER_DESCENDING = "desc"
private const val HTTP_NOT_FOUND = 404
private const val CONNECTION_ERROR_MESSAGE =
    "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo."
private const val SERVER_ERROR_MESSAGE =
    "El servidor no respondió correctamente. Inténtalo de nuevo."
private const val DATA_ERROR_MESSAGE =
    "No se pudo leer la información recibida del servidor."

class OrdersViewModel : ViewModel() {
    private val orderApi = OrderRemoteClient.orderApi

    private val _ordersUiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Initial)
    val ordersUiState: StateFlow<OrdersUiState> = _ordersUiState.asStateFlow()
    private val _orderDetailUiState =
        MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val orderDetailUiState: StateFlow<OrderDetailUiState> = _orderDetailUiState.asStateFlow()

    fun loadOrders() {
        _ordersUiState.value = OrdersUiState.Loading
        viewModelScope.launch {
            _ordersUiState.value = try {
                val orders = orderApi.getOrders(
                    sortBy = SORT_BY_CREATED_AT,
                    order = ORDER_DESCENDING
                )
                if (orders.isEmpty()) {
                    OrdersUiState.Empty
                } else {
                    OrdersUiState.Success(orders)
                }
            } catch (exception: IOException) {
                OrdersUiState.Error(CONNECTION_ERROR_MESSAGE)
            } catch (exception: HttpException) {
                OrdersUiState.Error(SERVER_ERROR_MESSAGE)
            } catch (exception: SerializationException) {
                OrdersUiState.Error(DATA_ERROR_MESSAGE)
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        _orderDetailUiState.value = OrderDetailUiState.Loading
        viewModelScope.launch {
            _orderDetailUiState.value = try {
                OrderDetailUiState.Success(orderApi.getOrder(id = orderId))
            } catch (exception: IOException) {
                OrderDetailUiState.Error(CONNECTION_ERROR_MESSAGE)
            } catch (exception: HttpException) {
                if (exception.code() == HTTP_NOT_FOUND) {
                    OrderDetailUiState.NotFound
                } else {
                    OrderDetailUiState.Error(SERVER_ERROR_MESSAGE)
                }
            } catch (exception: SerializationException) {
                OrderDetailUiState.Error(DATA_ERROR_MESSAGE)
            }
        }
    }
}
