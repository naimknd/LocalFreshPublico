package com.example.localfresh.model.comprador.detalle

data class DetalleUnidadResponse(
    val status: String,
    val message: String,
    val data: UnitData
)

data class UnitData(
    val unit: DetalleUnidad
)