package com.example.localfresh.model.comprador.paginaprincipal

data class TiendaData(
    val seller_id: Int,
    val email: String,
    val store_name: String,
    val store_description: String,
    val store_phone: String,
    val store_address: String,
    val latitude: Double,
    val longitude: Double,
    val organic_products: Int,
    val store_type: String,
    val store_logo: String,
    val opening_time: String,
    val closing_time: String,
    val store_rating: String
)