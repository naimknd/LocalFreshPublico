package com.example.localfresh.utils

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.localfresh.model.UpdateTokenRequest
import com.example.localfresh.model.UpdateTokenResponse
import com.example.localfresh.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TokenManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "LocalFreshPrefs"
        private const val KEY_FCM_TOKEN = "FCM_TOKEN"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Maneja un nuevo token FCM recibido
     */
    fun handleNewToken(token: String) {
        // 1. Guardar localmente
        saveTokenLocally(token)

        // 2. Enviarlo al servidor si hay usuario autenticado
        sendTokenToServer(token)
    }

    /**
     * Guarda el token FCM en SharedPreferences
     */
    fun saveTokenLocally(token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()

        Log.d(TAG, "Token FCM guardado localmente")
    }

    /**
     * Obtiene el token FCM guardado localmente
     */
    fun getLocalToken(): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Envía el token al servidor si hay un usuario autenticado
     */
    fun sendTokenToServer(token: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("TOKEN", null)

        val userId = if (sharedPreferences.getBoolean("IS_SELLER", false)) {
            sharedPreferences.getInt("SELLER_ID", -1)
        } else {
            sharedPreferences.getInt("USER_ID", -1)
        }

        if (authToken != null && userId != -1) {
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            updateTokenOnServer(userId, deviceId, token)
        } else {
            Log.d(TAG, "Usuario no autenticado, token no enviado al servidor")
        }
    }

    /**
     * Actualiza el token en el servidor con reintentos automáticos
     */
    private fun updateTokenOnServer(userId: Int, deviceId: String, fcmToken: String, retryCount: Int = 0) {
        val request = UpdateTokenRequest(userId, deviceId, fcmToken)

        RetrofitInstance.LocalFreshApiService.updateFcmToken(request)
            .enqueue(object : Callback<UpdateTokenResponse> {  // Cambiar el tipo aquí
                override fun onResponse(call: Call<UpdateTokenResponse>, response: Response<UpdateTokenResponse>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Token actualizado correctamente en el servidor")
                    } else {
                        Log.e(TAG, "Error al actualizar token: ${response.code()}")
                        if (retryCount < 3) retry(userId, deviceId, fcmToken, retryCount)
                    }
                }

                override fun onFailure(call: Call<UpdateTokenResponse>, t: Throwable) {
                    Log.e(TAG, "Error al actualizar token: ${t.message}")
                    if (retryCount < 3) retry(userId, deviceId, fcmToken, retryCount)
                }
            })
    }

    private fun retry(userId: Int, deviceId: String, fcmToken: String, retryCount: Int) {
        // Reintento con retraso exponencial
        CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay((1000 * (retryCount + 1)).toLong())
            updateTokenOnServer(userId, deviceId, fcmToken, retryCount + 1)
        }
    }
}