package com.example.localfresh.viewmodel.comprador.paginaprincipal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.comprador.paginaprincipal.PaginaPrincipalCompradorResponse
import com.example.localfresh.model.comprador.paginaprincipal.TiendasPaginaPrincipal
import com.example.localfresh.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.*

class PaginaPrincipalViewModel : ViewModel() {

    private val _tiendasResponse = MutableLiveData<PaginaPrincipalCompradorResponse?>()
    val tiendasResponse: LiveData<PaginaPrincipalCompradorResponse?> = _tiendasResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun obtenerTiendasYDistancia(
        token: String,
        storeType: String? = null,
        organic: Int? = null,
        distance: Int? = null
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                RetrofitInstance.compradorApiService.obtenerTiendasYDistancia(
                    storeType,
                    organic,
                    distance
                ).enqueue(object : Callback<PaginaPrincipalCompradorResponse> {
                    override fun onResponse(
                        call: Call<PaginaPrincipalCompradorResponse>,
                        response: Response<PaginaPrincipalCompradorResponse>
                    ) {
                        _isLoading.value = false
                        if (response.isSuccessful) {
                            _tiendasResponse.value = response.body()
                            Log.d("PaginaPrincipalViewModel", "Respuesta exitosa: ${response.body()}")
                        } else {
                            _error.value = "Error: ${response.message()}"
                            _tiendasResponse.value = null
                            Log.e("PaginaPrincipalViewModel", "Error en respuesta: ${response.code()} - ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<PaginaPrincipalCompradorResponse>, t: Throwable) {
                        _isLoading.value = false
                        _error.value = "Error de conexión: ${t.message}"
                        _tiendasResponse.value = null
                        Log.e("PaginaPrincipalViewModel", "Fallo en llamada: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error: ${e.message}"
                _tiendasResponse.value = null
                Log.e("PaginaPrincipalViewModel", "Excepción: ${e.message}")
            }
        }
    }

    fun filtrarTiendasEnRango(userLat: Double, userLon: Double, rangoDistancia: Double, tiendas: List<TiendasPaginaPrincipal>): List<TiendasPaginaPrincipal> {
        return tiendas.filter { tienda ->
            val distance = haversine(userLat, userLon, tienda.latitude, tienda.longitude)
            distance <= rangoDistancia * 1000
        }
    }

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Radio de la Tierra en metros
        val phi1 = lat1 * Math.PI / 180
        val phi2 = lat2 * Math.PI / 180
        val deltaPhi = (lat2 - lat1) * Math.PI / 180
        val deltaLambda = (lon2 - lon1) * Math.PI / 180

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // Distancia en metros
    }
}