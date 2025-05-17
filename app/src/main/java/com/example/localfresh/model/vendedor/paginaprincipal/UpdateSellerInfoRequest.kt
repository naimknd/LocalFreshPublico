package com.example.localfresh.model.vendedor.paginaprincipal

import java.io.File

data class UpdateSellerInfoRequest(
    val seller_id: Int,
    val store_name: String?,
    val store_description: String?,
    val store_phone: String?,
    val store_address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val opening_time: String?,
    val closing_time: String?,
    val store_logo: File? = null
)
