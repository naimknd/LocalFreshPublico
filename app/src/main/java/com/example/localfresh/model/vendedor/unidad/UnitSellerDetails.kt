package com.example.localfresh.model.vendedor.unidad

data class UnitSellerDetails(
    val unit_id: Int,
    val product_price: Double,
    val original_price: Double,
    val discount_price: Double?,
    val expiry_date: String,
    val status: String,
    val quantity: Int,
    val has_stock: Boolean
)