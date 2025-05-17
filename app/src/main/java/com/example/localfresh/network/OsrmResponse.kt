package com.example.localfresh.network

data class OsrmResponse(
    val routes: List<Route>,
    val code: String
)

data class Route(
    val distance: Double,
    val duration: Double
)