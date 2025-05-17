package com.example.localfresh.model.comprador.apartados

data class ReservationResponse(
    val status: String,
    val message: String,
    val expiration_message: String,
    val reservation: ReservationDetails,
    val items: List<ReservedUnit>
)

data class ReservationDetails(
    val reservation_id: Int,
    val user_id: Int,
    val seller_id: Int,
    val status: String,
    val total_price: Double,
    val reservation_date: String,
    val expiration_date: String,
    val qr_code: String,
    val store_name: String,
    val store_phone: String,
    val store_address: String
)

data class ReservedUnit(
    val unit_id: Int,
    val price: Double,
    val product_name: String,
    val image_url: String
)