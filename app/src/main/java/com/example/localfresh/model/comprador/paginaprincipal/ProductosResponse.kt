package com.example.localfresh.model.comprador.paginaprincipal

data class ProductosResponse(
    val status: String,
    val productos: List<ProductoData>
)