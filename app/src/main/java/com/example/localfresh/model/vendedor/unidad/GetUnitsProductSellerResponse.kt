package com.example.localfresh.model.vendedor.unidad

data class GetUnitsProductSellerResponse(
    val status: String,
    val product: ProductUnitSellerDetails?,
    val units: List<UnitSellerDetails>?,
    val message: String?
)