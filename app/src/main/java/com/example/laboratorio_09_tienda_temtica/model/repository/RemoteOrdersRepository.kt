package com.example.laboratorio_09_tienda_temtica.model.repository

import com.example.laboratorio_09_tienda_temtica.model.OrderSubmission
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder

interface RemoteOrdersRepository {
    suspend fun createOrder(order: OrderSubmission): Result<RemoteOrder>
    suspend fun getOrders(): Result<List<RemoteOrder>>
    suspend fun getOrder(id: String): Result<RemoteOrder>
}
