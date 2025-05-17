package com.example.localfresh.repository.comprador

import com.example.localfresh.model.comprador.apartados.CancelReservationRequest
import com.example.localfresh.model.comprador.apartados.CancelReservationResponse
import com.example.localfresh.model.comprador.apartados.ReservationDetailResponse
import com.example.localfresh.model.comprador.apartados.ReservationListResponse
import com.example.localfresh.model.comprador.apartados.ReservationRequest
import com.example.localfresh.model.comprador.apartados.ReservationResponse
import com.example.localfresh.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReservationRepositoryComprador {
    private val compradorApiService = RetrofitInstance.compradorApiService

    suspend fun confirmReservation(userId: Int, cartId: Int): Result<ReservationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = ReservationRequest(userId, cartId)
                val response = compradorApiService.confirmReservation(request).execute()

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

    suspend fun getReservations(
        userId: Int,
        status: String? = null,
        sortBy: String = "reservation_date",
        sortDirection: String = "DESC",
        history: String? = null
    ): Result<ReservationListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = compradorApiService.getReservations(
                userId = userId,
                status = status,
                sortBy = sortBy,
                sortDirection = sortDirection,
                history = history
            ).execute()

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

    suspend fun getReservationDetails(reservationId: Int): Result<ReservationDetailResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = compradorApiService.getReservationDetails(reservationId).execute()

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

    suspend fun cancelReservation(userId: Int, reservationId: Int): Result<CancelReservationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = CancelReservationRequest(userId, reservationId)
                val response = compradorApiService.cancelReservation(request).execute()

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
