package com.example.localfresh.model.comprador.favoritos


data class FavoritesResponse(
    val status: String,
    val message: String,
    val data: FavoritesData? = null
)