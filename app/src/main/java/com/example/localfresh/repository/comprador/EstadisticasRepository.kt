package com.example.localfresh.repository.comprador

import com.example.localfresh.model.comprador.estadisticas.EstadisticasResponse
import com.example.localfresh.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EstadisticasRepository {
    private val compradorApiService = RetrofitInstance.compradorApiService

    suspend fun getStatistics(
        userId: Int,
        period: String = "month",
        groupBy: String = "category"
    ): Result<EstadisticasResponse> = withContext(Dispatchers.IO) {
        try {
            val response = compradorApiService.getStatistics(userId, period, groupBy).execute()

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}