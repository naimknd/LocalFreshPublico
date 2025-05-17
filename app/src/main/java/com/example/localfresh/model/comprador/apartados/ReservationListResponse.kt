package com.example.localfresh.model.comprador.apartados

data class ReservationListResponse(
    val status: String,
    val count: Int,
    val reservations: List<ReservationData>
)

data class ReservationData(
    val reservation_id: Int,
    val status: String,
    val total_price: Double,
    val original_price: Double,
    val discount_amount: Double,
    val reservation_date: String,
    val expiration_date: String,
    val qr_code: String,
    val seller: SellerData,
    val user: UserData,
    val item_count: Int,
    val expired: Boolean,
    val formatted_date: String,
    val formatted_expiration: String
)

data class SellerData(
    val seller_id: Int,
    val store_name: String,
    val store_phone: String,
    val store_address: String,
    val store_logo: String,
    val opening_time: String,
    val closing_time: String,
    val store_is_open: Boolean?,
    val store_hours: String
)

data class UserData(
    val user_id: Int,
    val username: String,
    val phone: String,
    val email: String
)