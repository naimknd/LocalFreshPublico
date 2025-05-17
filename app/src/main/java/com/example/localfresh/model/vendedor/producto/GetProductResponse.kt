package com.example.localfresh.model.vendedor.producto

data class GetProductResponse(
    val status: String,
    val message: String,
    val data: InfoProductSeller?
)