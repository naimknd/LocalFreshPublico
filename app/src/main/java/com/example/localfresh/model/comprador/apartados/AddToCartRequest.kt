package com.example.localfresh.model.comprador.apartados

data class AddToCartRequest(
    val user_id: Int,
    val unit_id: Int,
    val quantity: Int = 1 // Cantidad por defecto
)