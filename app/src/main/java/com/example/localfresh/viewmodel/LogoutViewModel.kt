package com.example.localfresh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.LogoutRequest
import com.example.localfresh.model.LogoutResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogoutViewModel(application: Application) : AndroidViewModel(application) {
    val logoutResponse = MutableLiveData<LogoutResponse>()
    val errorMessage = MutableLiveData<String>()

    fun logout(token: String) {
        val request = LogoutRequest(token)
        val call = RetrofitInstance.LocalFreshApiService.logout(request)

        call.enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if (response.isSuccessful) {
                    logoutResponse.value = response.body()
                } else {
                    errorMessage.value = "Error al cerrar sesión: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                errorMessage.value = "Fallo de conexión: ${t.message}"
            }
        })
    }
}