package com.example.localfresh.viewmodel.vendedor.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.vendedor.signup.SignUpVendedorRequest
import com.example.localfresh.model.vendedor.signup.SignUpVendedorResponse
import com.example.localfresh.network.RetrofitInstance
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpVendedoresViewModel : ViewModel() {

    // LiveData para la respuesta de registro
    private val _registrationResponse = MutableLiveData<SignUpVendedorResponse>()
    val registrationResponse: LiveData<SignUpVendedorResponse> get() = _registrationResponse

    // LiveData para manejar errores
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Función para registrar un vendedor
    fun registerVendedor(request: SignUpVendedorRequest) {
        RetrofitInstance.vendedorApiService.registrarVendedor(request).enqueue(object : Callback<SignUpVendedorResponse> {
            override fun onResponse(call: Call<SignUpVendedorResponse>, response: Response<SignUpVendedorResponse>) {
                if (response.isSuccessful) {
                    // Si la respuesta es exitosa, actualiza el LiveData de respuesta
                    _registrationResponse.value = response.body()
                } else {
                    // Manejar el error de la respuesta
                    val errorResponse = response.errorBody()?.string()
                    val errorMessage = parseErrorResponse(errorResponse)
                    _errorMessage.value = errorMessage ?: "Error desconocido"
                }
            }

            override fun onFailure(call: Call<SignUpVendedorResponse>, t: Throwable) {
                // Manejar el fallo de la conexión
                _errorMessage.value = "Fallo en la conexión: ${t.message}"
            }
        })
    }

    private fun parseErrorResponse(errorBody: String?): String? {
        return errorBody?.let {
            try {
                val jsonObject = JSONObject(it)
                jsonObject.getString("message")
            } catch (e: Exception) {
                null
            }
        }
    }
}