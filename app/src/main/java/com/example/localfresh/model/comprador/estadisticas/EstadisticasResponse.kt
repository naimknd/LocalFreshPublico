package com.example.localfresh.model.comprador.estadisticas

data class EstadisticasResponse(
    val status: String,
    val summary: EstadisticasSummary,
    val grouped_data: List<GroupedData>,
    val timeline_data: List<TimelineData>
)

data class EstadisticasSummary(
    val total_spent: Double,
    val total_savings: Double,
    val savings_percentage: Int,
    val period: String,
    val start_date: String,
    val end_date: String
)

data class GroupedData(
    val group_name: String,
    val spent_amount: Double,
    val saved_amount: Double,
    val purchase_count: Int
)

data class TimelineData(
    val date: String,
    val spent_amount: Double,
    val saved_amount: Double
)