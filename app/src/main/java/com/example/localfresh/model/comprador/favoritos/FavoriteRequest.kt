package com.example.localfresh.model.comprador.favoritos

data class FavoriteRequest(
    val user_id: Int,
    val seller_id: Int? = null,
    val product_id: Int? = null
)