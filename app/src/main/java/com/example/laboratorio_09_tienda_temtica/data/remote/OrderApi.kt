package com.example.laboratorio_09_tienda_temtica.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {

    @POST("orders")
    suspend fun createOrder(
        @Body order: CreateOrderRequestDto
    ): OrderDto

    @GET("orders")
    suspend fun getOrders(
        @Query("sortBy") sortBy: String,
        @Query("order") order: String
    ): List<OrderDto>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") id: String
    ): OrderDto
}
