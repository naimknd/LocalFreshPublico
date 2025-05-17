package com.example.localfresh.model.comprador.apartados

data class CartResponse(
    val status: String,
    val message: String?,
    val cart: CartInfo?,
    val items: List<CartItem>
)

data class CartInfo(
    val cart_id: Int,
    val user_id: Int,
    val seller_id: Int,
    val status: String,
    val created_at: String,
    val expiration_time: String,
    val seller_name: String,
    val opening_time: String,
    val closing_time: String,
    val store_phone: String,
    val store_address: String,
    val store_logo: String,
    val expires_in_minutes: Int,
    val store_is_open: Boolean,
    val total: Double,
    val total_original_price: Double,
    val total_savings: Double,
    val total_savings_percentage: Int,
    val total_items: Int,
    val unique_items: Int,
    val store_hours: String
)

data class CartItem(
    val item_id: Int,
    val cart_id: Int,
    val unit_id: Int,
    val quantity: Int,
    val added_at: String,
    val product_id: Int,
    val status: String,
    val discount_price: Double,
    val expiry_date: String,
    val product_name: String,
    val description: String,
    val category: String,
    val image_url: String,
    val base_price: Double,
    val days_until_expiry: Int,
    val price: Double,
    val original_price: Double,
    val discount_percentage: Int,
    val discount_amount: Double,
    val item_total: Double,
    val item_original_total: Double
)