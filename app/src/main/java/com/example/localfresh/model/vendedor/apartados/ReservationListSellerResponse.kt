package com.example.localfresh.model.vendedor.apartados

data class ReservationListSellerResponse(
    val status: String,
    val count: Int,
    val reservations: List<ReservationSellerItem>
)

data class ReservationSellerItem(
    val reservation_id: Int,
    val status: String,
    val total_price: Double,
    val original_price: Double,
    val reservation_date: String,
    val expiration_date: String,
    val qr_code: String,
    val user: UserInfo,
    val item_count: Int,
    val expired: Boolean,
    val formatted_date: String,
    val formatted_expiration: String
)

data class UserInfo(
    val user_id: Int,
    val username: String,
    val full_name: String,
    val phone: String,
    val email: String
)