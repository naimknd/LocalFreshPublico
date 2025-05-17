package com.example.localfresh.model.vendedor.apartados

data class MarkReservationCompleteRequest(
    val reservation_id: Int,
    val seller_id: Int
)

data class MarkReservationCompleteResponse(
    val status: String,
    val message: String
)