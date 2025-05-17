package com.example.localfresh.viewmodel.comprador.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.comprador.PreferenciasCompradorRequest
import com.example.localfresh.network.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompletarPerfilViewModel : ViewModel() {

    private val _responseMessage = MutableLiveData<String>()
    val responseMessage: LiveData<String> get() = _responseMessage

    // Nuevo LiveData para las preferencias obtenidas
    private val _preferencias = MutableLiveData<PreferenciasCompradorRequest?>()
    val preferencias: LiveData<PreferenciasCompradorRequest?> get() = _preferencias

    // Indicador de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun guardarPreferencias(preferencias: PreferenciasCompradorRequest) {
        _isLoading.value = true
        val call = RetrofitInstance.compradorApiService.guardarPreferencias(preferencias)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _responseMessage.value = "Preferencias guardadas correctamente."
                } else {
                    _responseMessage.value = "Error al guardar las preferencias: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                _responseMessage.value = "Error: ${t.message}"
            }
        })
    }

    // Metodo para obtener preferencias
    fun obtenerPreferencias(userId: Int) {
        _isLoading.value = true
        RetrofitInstance.compradorApiService.getPreferencias(userId)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.get("status") == "success") {
                            val preferences = responseBody["preferences"] as? Map<*, *>
                            if (preferences != null) {
                                // Convertir el mapa a un objeto PreferenciasCompradorRequest
                                _preferencias.value = PreferenciasCompradorRequest(
                                    user_id = userId,
                                    preferencias_lacteos = (preferences["preferencias_lacteos"] as? Double)?.toInt() ?: 0,
                                    preferencias_carnes = (preferences["preferencias_carnes"] as? Double)?.toInt() ?: 0,
                                    preferencias_huevos = (preferences["preferencias_huevos"] as? Double)?.toInt() ?: 0,
                                    preferencias_granos_cereales = (preferences["preferencias_granos_cereales"] as? Double)?.toInt() ?: 0,
                                    preferencias_legumbres = (preferences["preferencias_legumbres"] as? Double)?.toInt() ?: 0,
                                    preferencias_panaderia = (preferences["preferencias_panaderia"] as? Double)?.toInt() ?: 0,
                                    preferencias_bebidas = (preferences["preferencias_bebidas"] as? Double)?.toInt() ?: 0,
                                    preferencias_snacks = (preferences["preferencias_snacks"] as? Double)?.toInt() ?: 0,
                                    preferencias_congelados = (preferences["preferencias_congelados"] as? Double)?.toInt() ?: 0,
                                    preferencias_comida_mascotas = (preferences["preferencias_comida_mascotas"] as? Double)?.toInt() ?: 0,
                                    rango_distancia = (preferences["rango_distancia"] as? Double)?.toInt() ?: 10
                                )
                            } else {
                                _responseMessage.value = "No se encontraron preferencias para este usuario"
                            }
                        } else {
                            _responseMessage.value = responseBody?.get("message")?.toString() ?: "Error al obtener preferencias"
                        }
                    } else {
                        _responseMessage.value = "Error: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    _isLoading.value = false
                    _responseMessage.value = "Error de conexión: ${t.message}"
                }
            })
    }
}