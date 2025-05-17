package com.example.localfresh.viewmodel.comprador.notificaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.comprador.notificaciones.NotificationPreferences
import com.example.localfresh.model.comprador.notificaciones.NotificationPreferencesRequest
import com.example.localfresh.model.comprador.notificaciones.NotificationPreferencesResponse
import com.example.localfresh.network.RetrofitInstance
import com.example.localfresh.utils.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreferenciasNotificacionesViewModel : ViewModel() {

    private val _preferences = MutableLiveData<NotificationPreferences>()
    val preferences: LiveData<NotificationPreferences> = _preferences

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateResult = MutableLiveData<Event<String>>()
    val updateResult: LiveData<Event<String>> = _updateResult

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    fun getNotificationPreferences(userId: Int) {
        _isLoading.value = true

        RetrofitInstance.compradorApiService.getNotificationPreferences(userId)
            .enqueue(object : Callback<NotificationPreferencesResponse> {
                override fun onResponse(
                    call: Call<NotificationPreferencesResponse>,
                    response: Response<NotificationPreferencesResponse>
                ) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            body.preferences?.let {
                                _preferences.value = it
                            } ?: run {
                                // Si no hay preferencias previas, establecer valores predeterminados
                                _preferences.value = NotificationPreferences(
                                    user_id = userId,
                                    frequency = "diaria",
                                    notif_ofertas = 1,
                                    notif_nuevos_productos = 1,
                                    notif_recordatorios = 1
                                )
                            }
                        } else {
                            _errorMessage.value = Event(body?.message ?: "Error desconocido")
                        }
                    } else {
                        _errorMessage.value = Event("Error de conexión: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<NotificationPreferencesResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = Event("Error de conexión: ${t.message}")
                }
            })
    }

    fun updateNotificationPreferences(request: NotificationPreferencesRequest) {
        _isLoading.value = true

        RetrofitInstance.compradorApiService.updateNotificationPreferences(request)
            .enqueue(object : Callback<NotificationPreferencesResponse> {
                override fun onResponse(
                    call: Call<NotificationPreferencesResponse>,
                    response: Response<NotificationPreferencesResponse>
                ) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            _updateResult.value = Event("Preferencias actualizadas correctamente")
                            body.preferences?.let {
                                _preferences.value = it
                            }
                        } else {
                            _errorMessage.value = Event(body?.message ?: "Error desconocido")
                        }
                    } else {
                        _errorMessage.value = Event("Error de conexión: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<NotificationPreferencesResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = Event("Error de conexión: ${t.message}")
                }
            })
    }
}