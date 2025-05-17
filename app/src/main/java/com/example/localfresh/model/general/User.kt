package com.example.localfresh.model.general

data class User(
    val user_id: Int,
    val email: String,
    val first_name: String,
    val last_name: String,
    val phone: String,
    val username: String,
    val birthdate: String,
    val first_time_login: Int,
    val is_verified: Int
)