package com.example.localfresh.model.comprador.reviews

data class ReportReviewRequest(
    val review_id: Int,
    val user_id: Int,
    val reason: String,
    val additional_info: String? = null
)

data class ReportReviewResponse(
    val status: String,
    val message: String
)