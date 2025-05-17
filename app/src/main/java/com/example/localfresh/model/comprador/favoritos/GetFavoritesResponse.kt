package com.example.localfresh.model.comprador.favoritos

data class GetFavoritesResponse(
    val status: String,
    val message: String? = null,
    val stores: List<StoreFavorite>? = null,
    val products: List<ProductFavorite>? = null
)