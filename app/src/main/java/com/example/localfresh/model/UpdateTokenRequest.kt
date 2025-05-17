package com.example.localfresh.model

data class UpdateTokenRequest(
    val user_id: Int,
    val device_id: String,
    val fcm_token: String
)

data class UpdateTokenResponse(
    val status: String,
    val message: String
)