package com.example.localfresh.model.comprador.apartados

data class ReservationDetailResponse(
    val status: String,
    val reservation: ReservationDetail
)

data class ReservationDetail(
    val reservation_id: Int,
    val status: String,
    val total_price: Double,
    val original_price: Double,
    val reservation_date: String,
    val expiration_date: String,
    val qr_code: String,
    val seller: SellerInfo,
    val user: UserInfo,
    val item_count: Int,
    val expired: Boolean,
    val formatted_date: String,
    val formatted_expiration: String,
    val products: List<ReservedProduct>,
    val total_savings: Double,
    val savings_percentage: Int
)

data class SellerInfo(
    val seller_id: Int,
    val store_name: String,
    val store_phone: String,
    val store_address: String,
    val store_logo: String,
    val latitude: Double,
    val longitude: Double,
    val opening_time: String,
    val closing_time: String,
    val store_is_open: Boolean,
    val store_hours: String
)

data class UserInfo(
    val user_id: Int,
    val username: String,
    val phone: String,
    val email: String
)

data class ReservedProduct(
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
    val total_price: Double,
    val total_original_price: Double,
    val expiry_date: String,
    val category: String
)