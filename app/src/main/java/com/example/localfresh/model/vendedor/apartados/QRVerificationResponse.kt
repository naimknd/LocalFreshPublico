package com.example.localfresh.model.vendedor.apartados

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QRVerificationResponse(
    val status: String,
    val message: String,
    val reservation: QRReservation
) : Parcelable

@Parcelize
data class QRReservation(
    val reservation_id: Int,
    val customer_name: String,
    val username: String,
    val email: String,
    val phone: String,
    val total_price: Double,
    val original_price: Double,
    val discount_amount: Double,
    val discount_percentage: Int,
    val reservation_date: String,
    val expiration_date: String,
    val status: String,
    val item_count: Int,
    val products: List<QRProduct>
) : Parcelable

@Parcelize
data class QRProduct(
    val unit_id: Int,
    val price: String,
    val quantity: Int,
    val product_name: String,
    val image_url: String,
    val description: String,
    val original_price: Double,
    val expiration_date: String,
    val discount_amount: Double,
    val discount_percentage: Int,
    val item_total: Double,
    val original_total: Double,
    val category: String
) : Parcelable