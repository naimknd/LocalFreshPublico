package com.example.localfresh.model.vendedor.unidad

data class ProductUnitSellerDetails(
    val product_id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val image_url: String?,
    val expiry_type: String,
    val total_quantity: Int,
    val available_quantity: Int
)