package com.example.localfresh.model.vendedor.paginaprincipal

data class UpdateSellerInfoResponse(
    val status: String, // Puede ser "success" o "error"
    val message: String, // Mensaje que describe el resultado de la operación
    val data: SellerStoreInfoClass? // Información actualizada de la tienda, si es necesario
)