package com.example.localfresh.model.general

data class Seller(
    val seller_id: Int,
    val store_name: String,
    val store_description: String,
    val store_phone: String,
    val store_address: String,
    val latitude: Double,
    val longitude: Double,
    val email: String,
    val is_verified: Int
)