package com.example.localfresh.viewmodel.comprador.recomendaciones

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.comprador.recomendaciones.RecommendationResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecommendationsViewModel : ViewModel() {
    private val _recommendations = MutableLiveData<RecommendationResponse>()
    val recommendations: LiveData<RecommendationResponse> = _recommendations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadRecommendations(userId: Int) {
        _isLoading.value = true
        RetrofitInstance.compradorApiService.getRecommendations(userId)
            .enqueue(object : Callback<RecommendationResponse> {
                override fun onResponse(
                    call: Call<RecommendationResponse>,
                    response: Response<RecommendationResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _recommendations.value = response.body()
                        Log.d("Recommendations", "Received ${response.body()?.recommendations?.size ?: 0} recommendations")
                    } else {
                        _error.value = "Error en la respuesta: ${response.code()}"
                        Log.e("Recommendations", "API Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<RecommendationResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value = "Error de conexión: ${t.message}"
                    Log.e("Recommendations", "Network error: ${t.message}")
                }
            })
    }
}