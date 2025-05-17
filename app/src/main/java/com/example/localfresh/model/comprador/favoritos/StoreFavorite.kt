package com.example.localfresh.model.comprador.favoritos

data class StoreFavorite(
    val seller_id: Int,
    val store_name: String,
    val store_description: String,
    val store_logo: String,
    val store_phone: String? = null,
    val store_address: String? = null,
    val opening_time: String? = null,
    val closing_time: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val store_rating: Double? = null
)