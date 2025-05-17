package com.example.localfresh.model.comprador.favoritos

data class FavoritesData(
    val stores: List<StoreFavorite>? = null,
    val products: List<ProductFavorite>? = null
)