package com.example.localfresh.repository.vendedor

import com.example.localfresh.model.vendedor.notificaciones.SellerNotificationsResponse
import com.example.localfresh.network.RetrofitInstance

class NotificationVendedorRepository {
    suspend fun getSellerNotifications(
        sellerId: Int,
        limit: Int = 20,
        offset: Int = 0,
        type: String? = null,
        isRead: Int? = null,
        sortBy: String = "created_at",
        sortDirection: String = "DESC"
    ): Result<SellerNotificationsResponse> {
        return try {
            val response = RetrofitInstance.vendedorApiService.getSellerNotifications(
                sellerId, limit, offset, type, isRead, sortBy, sortDirection
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al obtener notificaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}