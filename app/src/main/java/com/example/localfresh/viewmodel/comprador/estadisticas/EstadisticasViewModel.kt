package com.example.localfresh.viewmodel.comprador.estadisticas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.comprador.estadisticas.EstadisticasResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EstadisticasViewModel : ViewModel() {

    private val _estadisticasData = MutableLiveData<EstadisticasResponse>()
    val estadisticasData: LiveData<EstadisticasResponse> = _estadisticasData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadStatistics(userId: Int, period: String, groupBy: String) {
        _isLoading.value = true
        _error.value = ""

        RetrofitInstance.compradorApiService.getStatistics(userId, period, groupBy)
            .enqueue(object : Callback<EstadisticasResponse> {
                override fun onResponse(
                    call: Call<EstadisticasResponse>,
                    response: Response<EstadisticasResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _estadisticasData.value = it
                        } ?: run {
                            _error.value = "Error: Respuesta vacía del servidor"
                        }
                    } else {
                        _error.value = "Error: ${response.code()} - ${response.message()}"
                    }
                }

                override fun onFailure(call: Call<EstadisticasResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value = "Error de conexión: ${t.message}"
                }
            })
    }
}