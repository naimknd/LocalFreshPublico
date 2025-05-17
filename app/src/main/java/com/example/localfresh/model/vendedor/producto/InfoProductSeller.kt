package com.example.localfresh.model.vendedor.producto

data class InfoProductSeller(
    val product_id: Int,
    val seller_id: Int?,
    val name: String?,
    val description: String?,
    val category: String?,
    val price: Double?,
    val image_url: String?,
    val expiry_type: String?,
    val unit_count: Int = 0,
    val total_quantity: Int = 0,
    val available_quantity: Int = 0,
    val reserved_quantity: Int = 0,
    val expired_quantity: Int = 0,
    val has_stock: Boolean = false,
    val status_breakdown: StatusBreakdown? = null
)

data class StatusBreakdown(
    val disponible: Int = 0,
    val apartado: Int = 0,
    val expirado: Int = 0
)