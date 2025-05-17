package com.example.localfresh.api

import com.example.localfresh.model.LoginRequest
import com.example.localfresh.model.LoginResponse
import com.example.localfresh.model.LogoutRequest
import com.example.localfresh.model.LogoutResponse
import com.example.localfresh.model.PasswordResetRequest
import com.example.localfresh.model.PasswordResetResponse
import com.example.localfresh.model.UpdateTokenRequest
import com.example.localfresh.model.UpdateTokenResponse
import com.example.localfresh.model.ValidationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiLocalFresh {

    // Metodo para el inicio de sesión
    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Metodo para cerrar sesión
    @POST("logout.php")
    fun logout(@Body request: LogoutRequest): Call<LogoutResponse>


    // Metodo para solicitar el restablecimiento de contraseña
    @POST("request_password_reset.php")
    fun requestPasswordReset(@Body request: PasswordResetRequest): Call<PasswordResetResponse>

    @POST("validate_token.php")
    fun validateToken(@Header("Authorization") token: String): Call<ValidationResponse>

    // Metodo para actualizar el token de FCM
    @POST("update_fcm_token.php")
    fun updateFcmToken(@Body request: UpdateTokenRequest): Call<UpdateTokenResponse>
}