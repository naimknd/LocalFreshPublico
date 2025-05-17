package com.example.localfresh.model.comprador.paginaprincipal

data class ProductoData(
    val product_id: Int,
    val product_name: String,
    val product_description: String,
    val product_category: String,
    val product_price: Double,
    val product_image: String,
    val expiry_type: String,
    val units: List<UnitData>
)