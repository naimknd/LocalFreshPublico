package com.example.localfresh.model.vendedor.unidad

data class GetUnitResponse(
    val status: String,
    val data: UnitSellerDetails?,
    val message: String?
)