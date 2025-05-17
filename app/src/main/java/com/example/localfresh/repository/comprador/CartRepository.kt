package com.example.localfresh.repository.comprador

import com.example.localfresh.model.comprador.apartados.AddToCartRequest
import com.example.localfresh.model.comprador.apartados.AddToCartResponse
import com.example.localfresh.model.comprador.apartados.CartResponse
import com.example.localfresh.network.RetrofitInstance
import com.example.localfresh.model.comprador.apartados.RemoveFromCartRequest
import com.example.localfresh.model.comprador.apartados.RemoveFromCartResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository {
    private val compradorApiService = RetrofitInstance.compradorApiService

    suspend fun addToCart(userId: Int, unitId: Int, quantity: Int = 1): Result<AddToCartResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AddToCartRequest(userId, unitId, quantity)
            val response = compradorApiService.addToCart(request).execute()

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

    suspend fun getCart(userId: Int, cartId: Int? = null): Result<CartResponse> = withContext(Dispatchers.IO) {
        try {
            val response = compradorApiService.getCart(userId, cartId).execute()

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

    suspend fun removeFromCart(userId: Int, unitId: Int, quantity: Int? = null): Result<RemoveFromCartResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = RemoveFromCartRequest(userId, unitId, quantity)
                val response = compradorApiService.removeFromCart(request).execute()

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
