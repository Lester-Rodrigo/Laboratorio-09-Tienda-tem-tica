package com.example.laboratorio_09_tienda_temtica.data.remote

import com.example.laboratorio_09_tienda_temtica.model.OrderSubmission
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrder
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderException
import com.example.laboratorio_09_tienda_temtica.model.RemoteOrderFailure
import com.example.laboratorio_09_tienda_temtica.model.repository.RemoteOrdersRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class RetrofitRemoteOrdersRepository(private val api: OrderApi) : RemoteOrdersRepository {
    override suspend fun createOrder(order: OrderSubmission): Result<RemoteOrder> = request {
        api.createOrder(order.toRequestDto()).toDomain()
    }

    override suspend fun getOrders(): Result<List<RemoteOrder>> = request {
        api.getOrders(sortBy = "createdAt", order = "desc").map { it.toDomain() }
    }

    override suspend fun getOrder(id: String): Result<RemoteOrder> = request {
        api.getOrder(id).toDomain()
    }

    private suspend fun <T> request(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        val reason = when (exception) {
            is IOException -> RemoteOrderFailure.CONNECTION
            is HttpException -> if (exception.code() == 404) RemoteOrderFailure.NOT_FOUND else RemoteOrderFailure.SERVER
            is SerializationException, is NumberFormatException, is ArithmeticException -> RemoteOrderFailure.INVALID_DATA
            else -> RemoteOrderFailure.SERVER
        }
        Result.failure(RemoteOrderException(reason))
    }
}
