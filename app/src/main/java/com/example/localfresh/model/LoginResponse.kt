package com.example.localfresh.model

import com.example.localfresh.model.general.Seller
import com.example.localfresh.model.general.User

data class LoginResponse(
    val status: String,
    val message: String,
    val token: String?,
    val seller: Seller? = null,
    val user: User? = null,
    val code: Int? = null
)
