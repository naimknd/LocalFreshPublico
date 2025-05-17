package com.example.localfresh.viewmodel.comprador.signup

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.comprador.SignUpCompradorRequest
import com.example.localfresh.model.comprador.SignUpCompradorResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpCompradorViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData para manejar el estado de la respuesta
    val signUpResponse = MutableLiveData<String>()
    val registrationSuccess = MutableLiveData<Boolean>()
    val resendResponse = MutableLiveData<String>() // LiveData para la respuesta de reenvío
    var userEmail: String? = null // Propiedad para almacenar el correo electrónico

    // Metodo para registrar comprador
    fun signUpComprador(request: SignUpCompradorRequest) {
        val call = RetrofitInstance.compradorApiService.registrarComprador(request)

        call.enqueue(object : Callback<SignUpCompradorResponse> {
            override fun onResponse(
                call: Call<SignUpCompradorResponse>,
                response: Response<SignUpCompradorResponse>
            ) {
                if (response.isSuccessful) {
                    val registroResponse = response.body()
                    if (registroResponse != null) {
                        // Actualizar el LiveData con el mensaje
                        signUpResponse.value = registroResponse.message
                        registrationSuccess.value = registroResponse.status == "success" // Indica si el registro fue exitoso
                        if (registrationSuccess.value == true) {
                            userEmail = request.email // Almacenar el correo electrónico para caso de reenvio
                        }
                    } else {
                        signUpResponse.value = "Respuesta vacía del servidor" // Si el cuerpo es null
                        Log.e("SignUpComprador", "Respuesta vacía del servidor")
                    }
                } else {
                    // Manejo de errores según el código de respuesta
                    when (response.code()) {
                        400 -> signUpResponse.value = "Faltan campos obligatorios."
                        409 -> signUpResponse.value = "El correo electrónico ya está registrado. Intenta con otro"
                        500 -> signUpResponse.value = "Error interno del servidor. Intenta más tarde."
                        else -> signUpResponse.value = "Error en el registro: ${response.message()}"
                    }
                    Log.e("SignUpComprador", "Error en el registro: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SignUpCompradorResponse>, t: Throwable) {
                // Error de conexión
                signUpResponse.value = "Fallo la conexión: ${t.message}"
                Log.e("SignUpComprador", "Fallo la conexión: ${t.message}")  // Log del error
            }
        })
    }
}