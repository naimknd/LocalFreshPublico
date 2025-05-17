package com.example.localfresh.repository.vendedor

import com.example.localfresh.model.vendedor.apartados.MarkReservationCompleteRequest
import com.example.localfresh.model.vendedor.apartados.MarkReservationCompleteResponse
import com.example.localfresh.model.vendedor.apartados.QRVerificationResponse
import com.example.localfresh.model.vendedor.apartados.ReservationDetailSellerResponse
import com.example.localfresh.model.vendedor.apartados.ReservationListSellerResponse
import com.example.localfresh.network.RetrofitInstance

class ReservationSellerRepository {
    // Endpoint para obtener la lista de reservaciones del vendedor
    suspend fun getSellerReservations(
        sellerId: Int,
        status: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        buyerSearch: String? = null,
        isHistory: Boolean = false
    ): Result<ReservationListSellerResponse> {
        return try {
            val response = RetrofitInstance.vendedorApiService.getSellerReservations(
                sellerId,
                status,
                dateFrom,
                dateTo,
                buyerSearch,
                if (isHistory) "true" else null
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al obtener apartados"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Endpoint para obtener los detalles de una reservación
    suspend fun getSellerReservationDetails(reservationId: Int, sellerId: Int): Result<ReservationDetailSellerResponse> {
        return try {
            val response = RetrofitInstance.vendedorApiService.getSellerReservationDetails(reservationId, sellerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Error al obtener los detalles del apartado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Endpoint para verificar un código QR
    suspend fun verifyReservationQR(qrCode: String, sellerId: Int): Result<QRVerificationResponse> {
        return try {
            val response = RetrofitInstance.vendedorApiService.verifyReservationQR(qrCode, sellerId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al verificar el código QR"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Endpoint para marcar un apartado como completado
    suspend fun markReservationComplete(
        reservationId: Int,
        sellerId: Int
    ): Result<MarkReservationCompleteResponse> {
        return try {
            val request = MarkReservationCompleteRequest(reservationId, sellerId)
            val response = RetrofitInstance.vendedorApiService.markReservationComplete(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al marcar como completado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
