package com.example.localfresh.model.vendedor.notificaciones

data class ProductExpiryInfo(
    val product_id: Int,
    val unit_id: Int,
    val name: String,
    val category: String,
    val expiry_date: String,
    val expiry_type: String,
    val quantity: Int
)