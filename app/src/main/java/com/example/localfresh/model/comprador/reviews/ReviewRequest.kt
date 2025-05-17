package com.example.localfresh.model.comprador.reviews

data class ReviewRequest(
    val user_id: Int,
    val seller_id: Int,
    val reservation_id: Int,
    val rating: Int,
    val comment: String?
)

data class ReviewResponse(
    val status: String,
    val message: String
)