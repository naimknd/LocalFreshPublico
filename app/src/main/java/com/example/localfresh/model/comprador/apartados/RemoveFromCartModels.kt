package com.example.localfresh.model.comprador.apartados

data class RemoveFromCartRequest(
    val user_id: Int,
    val unit_id: Int,
    val quantity: Int? = null  // Cantidad opcional, null eliminara el producto
)

data class RemoveFromCartResponse(
    val status: String,
    val message: String,
    val items_remaining: Int
)