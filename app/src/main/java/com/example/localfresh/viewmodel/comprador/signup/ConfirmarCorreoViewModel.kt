package com.example.localfresh.viewmodel.comprador.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.comprador.ResendVerificationRequest
import com.example.localfresh.model.comprador.ResendVerificationResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmarCorreoViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitInstance.compradorApiService
    val resendResponse = MutableLiveData<ResendVerificationResponse>()

    fun resendVerificationEmail(email: String) {
        val request = ResendVerificationRequest(email)
        val call = apiService.reenviarCorreoVerificacion(request)

        call.enqueue(object : Callback<ResendVerificationResponse> {
            override fun onResponse(
                call: Call<ResendVerificationResponse>,
                response: Response<ResendVerificationResponse>
            ) {
                if (response.isSuccessful) {
                    resendResponse.value = response.body() ?: ResendVerificationResponse(
                        "error",
                        "Respuesta vacía del servidor"
                    )
                } else {
                    // Manejo de errores según el código de respuesta
                    when (response.code()) {
                        400 -> resendResponse.value = ResendVerificationResponse(
                            "error",
                            "Solicitud incorrecta. Verifica los datos."
                        )
                        404 -> resendResponse.value = ResendVerificationResponse(
                            "error",
                            "No se encontró un usuario con ese correo electrónico."
                        )
                        500 -> resendResponse.value = ResendVerificationResponse(
                            "error",
                            "Error interno del servidor. Intenta más tarde."
                        )
                        else -> resendResponse.value = ResendVerificationResponse(
                            "error",
                            "Error al reenviar el correo: ${response.message()}"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ResendVerificationResponse>, t: Throwable) {
                // Manejo de error de conexión
                resendResponse.value = ResendVerificationResponse(
                    "error",
                    "Fallo de conexión: ${t.message}"
                )
            }
        })
    }
}