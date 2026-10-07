package com.example.quakeapplication.domain.model

// An earthquake paired with how far it is from the user
// Distance is not stored on Earthquake, because it changes when the user moves
data class EarthquakeWithDistance(
    val earthquake: Earthquake,
    // Null when we do not know where the user is
    val distanceKm: Double?,
)
