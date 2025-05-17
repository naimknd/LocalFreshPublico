package com.example.localfresh.model.comprador.reviews

data class SellerReviewsResponse(
    val status: String,
    val seller_id: Int,
    val average_rating: Float,
    val total_reviews: Int,
    val distribution: Map<String, Int>,
    val reviews: List<ReviewItem>,
    val current_page: Int,
    val total_pages: Int
)

data class ReviewItem(
    val review_id: Int,
    val user_id: Int,
    val rating: Int,
    val comment: String?,
    val created_at: String,
    val username: String,
    val formatted_date: String,
    val user_display_name: String
)