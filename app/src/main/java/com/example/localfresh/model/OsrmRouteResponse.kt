package com.example.localfresh.model

data class OsrmRouteResponse(
    val code: String,
    val routes: List<Route>,
    val waypoints: List<Waypoint>
) {
    data class Route(
        val distance: Double,
        val duration: Double
    )

    data class Waypoint(
        val name: String,
        val location: List<Double>
    )
}