package com.example.localfresh.model.comprador.apartados

data class AddToCartResponse(
    val status: String, // "success" o "error"
    val message: String, // Mensaje de éxito o error
    val cart_id: Int, // ID del carrito
    val expiration_time: String, // Fecha de expiración
    val expires_in_minutes: Int, // Minutos para que expire
    val quantity: Int,           // Cantidad añadida
    val remaining_stock: Int     // Stock restante
)