package com.example.localfresh.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Obtener token de SharedPreferences
        val sharedPreferences = context.getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        // Si hay token disponible, añadirlo como encabezado
        return if (!token.isNullOrEmpty()) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            // Si no hay token, proceder con la solicitud original
            chain.proceed(originalRequest)
        }
    }
}