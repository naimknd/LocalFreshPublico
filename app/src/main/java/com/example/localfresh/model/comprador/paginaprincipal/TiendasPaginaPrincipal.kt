package com.example.localfresh.model.comprador.paginaprincipal

data class TiendasPaginaPrincipal(
    val seller_id: Int,
    val store_name: String,
    val latitude: Double,
    val longitude: Double,
    val opening_time: String,
    val closing_time: String,
    val organic_products: Int,
    val store_type: String
)