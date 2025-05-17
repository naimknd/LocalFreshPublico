package com.example.localfresh.model.vendedor.producto

import java.io.File

data class AddProductRequest(
    val seller_id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val image_file: File?, //File porque es imagen
    val expiry_type: String // Tipo de expiracion
)