package com.example.localfresh.model.comprador.paginaprincipal

data class PaginaPrincipalCompradorResponse(
    val status: String,
    val rango_distancia: Int,
    val tiendas: List<TiendasPaginaPrincipal>,
    val hora_actual: String,
    val filtros_aplicados: Map<String, Any?>? = null
)