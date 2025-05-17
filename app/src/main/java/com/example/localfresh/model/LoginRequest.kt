package com.example.localfresh.model

data class LoginRequest(
    val email: String,
    val password: String,
    val device_id: String, // Campo para el ID del dispositivo
    val fcm_token: String? = null // Campo para el token de FCM
)