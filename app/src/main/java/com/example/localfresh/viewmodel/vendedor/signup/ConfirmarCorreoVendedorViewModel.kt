package com.example.localfresh.viewmodel.vendedor.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.vendedor.signup.ResendVerificationVendedorRequest
import com.example.localfresh.model.vendedor.signup.ResendVerificationVendedorResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmarCorreoVendedorViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitInstance.vendedorApiService
    val resendResponse = MutableLiveData<ResendVerificationVendedorResponse>()

    fun resendVerificationEmail(email: String) {
        val request = ResendVerificationVendedorRequest(email)
        val call = apiService.reenviarCorreoVerificacion(request)

        call.enqueue(object : Callback<ResendVerificationVendedorResponse> {
            override fun onResponse(
                call: Call<ResendVerificationVendedorResponse>,
                response: Response<ResendVerificationVendedorResponse>
            ) {
                if (response.isSuccessful) {
                    resendResponse.value = response.body() ?: ResendVerificationVendedorResponse(
                        "error",
                        "Respuesta vacía del servidor"
                    )
                } else {
                    // Manejo de errores según el código de respuesta
                    when (response.code()) {
                        400 -> resendResponse.value = ResendVerificationVendedorResponse(
                            "error",
                            "Solicitud incorrecta. Verifica los datos."
                        )
                        404 -> resendResponse.value = ResendVerificationVendedorResponse(
                            "error",
                            "No se encontró un vendedor con ese correo electrónico."
                        )
                        500 -> resendResponse.value = ResendVerificationVendedorResponse(
                            "error",
                            "Error interno del servidor. Intenta más tarde."
                        )
                        else -> resendResponse.value = ResendVerificationVendedorResponse(
                            "error",
                            "Error al reenviar el correo: ${response.message()}"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ResendVerificationVendedorResponse>, t: Throwable) {
                // Manejo de error de conexión
                resendResponse.value = ResendVerificationVendedorResponse(
                    "error",
                    "Fallo de conexión: ${t.message}"
                )
            }
        })
    }
}