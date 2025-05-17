package com.example.localfresh.model.vendedor.producto

data class GetSellerProductsResponse(
    val status: String,                 // Estado de la respuesta (ej. "success" o "error")
    val message: String?,               // Mensaje adicional (opcional)
    val data: List<InfoProductSeller>?  // Lista de productos (puede ser nula)
)
