package com.example.localfresh.model.vendedor.unidad

data class AddUnitRequest(
    val product_id: Int,
    val discount_price: Double?,
    val expiry_date: String,
    val status: String,
    val quantity: Int
)