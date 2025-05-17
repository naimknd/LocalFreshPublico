package com.example.localfresh.network

import android.content.Context
import com.example.localfresh.api.ApiLocalFresh
import com.example.localfresh.api.CompradorApiService
import com.example.localfresh.api.VendedorApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.100.97/localfresh/"

    private var retrofit: Retrofit? = null

    // Inicializar la instancia con el contexto de la aplicación
    fun initialize(context: Context) {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(context))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val compradorApiService: CompradorApiService by lazy {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitInstance no inicializado. Llama a initialize() primero.")
        }
        retrofit!!.create(CompradorApiService::class.java)
    }

    val vendedorApiService: VendedorApiService by lazy {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitInstance no inicializado. Llama a initialize() primero.")
        }
        retrofit!!.create(VendedorApiService::class.java)
    }

    val LocalFreshApiService: ApiLocalFresh by lazy {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitInstance no inicializado. Llama a initialize() primero.")
        }
        retrofit!!.create(ApiLocalFresh::class.java)
    }
}