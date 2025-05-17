package com.example.localfresh.model

data class PasswordResetResponse(
    val status: String,
    val code: Int? = null,
    val message: String
)