package com.example.trucktrack.model

data class OsrmRouteResponse(
    val routes: List<Route>,
    val code: String
)

data class Route(
    val geometry: Geometry,
    val distance: Double,
    val duration: Double
)

data class Geometry(
    val coordinates: List<List<Double>>, // [lon, lat]
    val type: String
)

