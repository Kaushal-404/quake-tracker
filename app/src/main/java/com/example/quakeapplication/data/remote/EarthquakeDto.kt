package com.example.quakeapplication.data.remote

import kotlinx.serialization.Serializable
@Serializable
data class EarthquakeDto(
    // Each "feature" is one earthquake
    val features: List<FeatureDto>? = null,
)

@Serializable
data class FeatureDto(
    // The USGS event id, for example "nc75012345"
    val id: String? = null,
    // The facts about the earthquake: size, place, time
    val properties: PropertiesDto? = null,
    // Where it happened
    val geometry: GeometryDto? = null,
)

@Serializable
data class PropertiesDto(
    // Magnitude. USGS really does send null here for some new events
    val mag: Double? = null,
    // Free text such as "10 km NE of Ridgecrest, CA"
    val place: String? = null,
    // When it happened, as milliseconds since 1 January 1970 UTC
    val time: Long? = null,
    // Link to the USGS page for this event
    val url: String? = null,
    // USGS sends 1 when the event is flagged for tsunami risk, otherwise 0
    val tsunami: Int? = null,
)

@Serializable
data class GeometryDto(
    // A list of three numbers in this fixed order: longitude, latitude, depth in km
    // Longitude comes FIRST, which is the opposite of how people usually say it
    // The mapper turns this list into named fields so nobody else has to remember the order
    val coordinates: List<Double?>? = null,
)
