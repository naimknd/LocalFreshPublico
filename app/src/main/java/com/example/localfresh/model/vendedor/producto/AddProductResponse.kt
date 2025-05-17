package com.example.localfresh.model.vendedor.producto

data class AddProductResponse(
    val status: String, // "success", "error", or "info"
    val message: String // Mensaje que describe el resultado
)