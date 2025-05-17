package com.example.localfresh.model.comprador.apartados

data class CancelReservationRequest(
    val user_id: Int,
    val reservation_id: Int
)

data class CancelReservationResponse(
    val status: String,
    val message: String,
    val store_name: String?
)