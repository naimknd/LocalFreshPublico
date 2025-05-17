package com.example.localfresh.model.vendedor.apartados

data class ReservationDetailSellerResponse(
    val status: String,
    val reservation: ReservationDetailSeller
)

data class ReservationDetailSeller(
    val reservation_id: Int,
    val status: String,
    val total_price: Double,
    val original_price: Double,
    val reservation_date: String,
    val expiration_date: String,
    val qr_code: String,
    val user: ReservationUserInfo,
    val item_count: Int,
    val expired: Boolean,
    val formatted_date: String,
    val formatted_expiration: String,
    val products: List<ReservedProductSeller>,
    val total_savings: Double,
    val savings_percentage: Int
)

data class ReservationUserInfo(
    val user_id: Int,
    val username: String,
    val first_name: String,
    val last_name: String,
    val full_name: String,
    val phone: String,
    val email: String
)

data class ReservedProductSeller(
    val product_id: Int,
    val product_name: String,
    val product_image: String,
    val unit_id: Int,
    val reservation_unit_id: Int,
    val original_price: Double,
    val discount_price: Double,
    val discount_amount: Double,
    val discount_percentage: Int,
    val quantity: Int,
    val total_original_price: Double,
    val total_price: Double,
    val expiry_date: String,
    val formatted_expiry: String,
    val category: String
)