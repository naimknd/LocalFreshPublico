package com.example.localfresh.model.comprador

data class SignUpCompradorRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String?,
    val birthdate: String,
    val phone: String,
    val firstTimeLogin: Int = 1
)
