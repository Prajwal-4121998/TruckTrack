package com.example.trucktrack.model

data class OverpassResponse(val elements: List<OverpassElement>)
data class OverpassElement(
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val tags: Map<String, String>?
)
