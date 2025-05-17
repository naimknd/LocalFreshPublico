package com.example.localfresh.model.comprador.detalle

data class DetalleUnidad(
    val unit_id: Int,
    val discount_price: Double,
    val expiry_date: String,
    val status: String,
    val quantity: Int,
    val product_id: Int,
    val product_name: String,
    val product_description: String,
    val product_category: String,
    val product_price: Double,
    val product_image: String,
    val expiry_type: String,
    val seller_id: Int,
    val store_name: String,
    val latitude: Double,
    val longitude: Double
)