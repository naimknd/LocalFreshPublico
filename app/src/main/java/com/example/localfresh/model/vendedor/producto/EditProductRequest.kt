package com.example.localfresh.model.vendedor.producto

import java.io.File

data class EditProductRequest(
    val product_id: Int,
    val seller_id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val image_file: File?,
    val expiry_type: String
)