package com.example.localfresh.model.comprador.recomendaciones

data class RecommendationResponse(
    val status: String,
    val recommendations: List<RecommendedProduct>
)

data class RecommendedProduct(
    val product_id: Int,
    val product_name: String,
    val product_description: String,
    val product_category: String,
    val product_price: Double,
    val discount_price: Double,
    val discount_percentage: Int,
    val product_image: String,
    val expiry_date: String,
    val days_until_expiry: Int,
    val unit_id: Int,
    val quantity: Int,
    val store_name: String,
    val store_rating: Float,
    val seller_id: Int,
    val freshness_score: Double,
    val preference_score: Double,
    val popularity_score: Double,
    val store_rating_score: Double,
    val total_quality: Double,
    val recommendation_reason: String
)