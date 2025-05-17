package com.example.localfresh.model.comprador.paginaprincipal

data class UnitData(
    val unit_id: Int,
    val discount_price: Double,
    val expiry_date: String,
    val status: String,
    val quantity: Int
)