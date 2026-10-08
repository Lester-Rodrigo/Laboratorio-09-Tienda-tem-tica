package com.example.laboratorio_09_tienda_temtica.data.remote

import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object OrderRemoteClient {
    private const val BASE_URL =
        "https://6ac7e57875a4ce3fe7225859.mockapi.io/api/v1/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(
            networkJson.asConverterFactory("application/json".toMediaType())
        )
        .build()

    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
}
