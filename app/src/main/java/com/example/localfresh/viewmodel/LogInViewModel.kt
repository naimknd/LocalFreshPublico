package com.example.localfresh.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.network.RetrofitInstance
import com.example.localfresh.model.LoginRequest
import com.example.localfresh.model.LoginResponse
import com.example.localfresh.model.ValidationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

class LogInViewModel(application: Application) : AndroidViewModel(application) {
    private val contextRef = WeakReference(application.applicationContext)
    val loginResponse = MutableLiveData<LoginResponse>()
    val errorMessage = MutableLiveData<String>()
    val tokenValidation = MutableLiveData<Boolean>()
    val networkStatus = MutableLiveData<Boolean>()

    fun login(email: String, password: String, deviceId: String, fcmToken: String? = null) {
        if (!isNetworkAvailable()) {
            errorMessage.value = "No hay conexión a internet"
            networkStatus.value = false
            return
        }

        val request = LoginRequest(email, password, deviceId, fcmToken)
        RetrofitInstance.LocalFreshApiService.login(request)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        loginResponse.value = response.body()
                        Log.d("LogInViewModel", "Login successful: ${response.body()}")
                    } else {
                        errorMessage.value = "Error en el inicio de sesión"
                        Log.e("LogInViewModel", "Login failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("Login", "Error de parseo: ${t.message}", t)
                }
            })
    }

    fun validateToken(token: String) {
        if (!isNetworkAvailable()) {
            errorMessage.value = "No hay conexión a internet"
            networkStatus.value = false
            tokenValidation.value = false
            return
        }

        RetrofitInstance.LocalFreshApiService.validateToken(token)
            .enqueue(object : Callback<ValidationResponse> {
                override fun onResponse(
                    call: Call<ValidationResponse>,
                    response: Response<ValidationResponse>
                ) {
                    val isValid = response.isSuccessful && response.body()?.status == "success"
                    tokenValidation.value = isValid
                    networkStatus.value = true

                    if (!isValid) {
                        Log.e("LogInViewModel", "Token inválido: ${response.body()?.message}")
                    }
                }

                override fun onFailure(call: Call<ValidationResponse>, t: Throwable) {
                    errorMessage.value = "Error de conexión: ${t.message}"
                    tokenValidation.value = false
                    networkStatus.value = false
                }
            })
    }

    private fun isNetworkAvailable(): Boolean {
        val context = contextRef.get() ?: return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCleared() {
        super.onCleared()
        contextRef.clear()
    }
}