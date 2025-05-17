package com.example.localfresh.model.comprador.perfil

/**
 * Respuesta a la petición de obtener información del perfil de usuario
 */
data class UserProfileResponse(
    val status: String,
    val user: User
)

/**
 * Modelo que representa la información del usuario
 */
data class User(
    val user_id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val birthdate: String,
    val phone: String
)

/**
 * Solicitud para actualizar la información del perfil de usuario
 */
data class UpdateUserProfileRequest(
    val user_id: Int,
    val username: String? = null,
    val email: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val birthdate: String? = null,
    val phone: String? = null
)

/**
 * Respuesta a la petición de actualizar información del perfil
 */
data class UpdateUserProfileResponse(
    val status: String,
    val message: String
)