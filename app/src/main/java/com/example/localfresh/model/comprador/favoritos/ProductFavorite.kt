package com.example.localfresh.model.comprador.favoritos

data class ProductFavorite(
    val product_id: Int,
    val product_name: String,
    val product_description: String,
    val product_image: String,
    val product_price: Double? = null,
    val category: String? = null,
    val seller_id: Int? = null,
    val store_name: String? = null,
    val available_units_count: Int? = null,
    val min_discount_price: Double? = null,
    val nearest_expiry: String? = null,
    val has_discount: Boolean? = null,
    val discount_percentage: Int? = null,
    val available: Boolean? = null,
    val unit_id: Int
)