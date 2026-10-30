package com.example.laboratorio_09_tienda_temtica.data

import kotlinx.coroutines.CancellationException

internal suspend fun persistenceResult(action: suspend () -> Unit): Result<Unit> = try {
    action()
    Result.success(Unit)
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    Result.failure(exception)
}
