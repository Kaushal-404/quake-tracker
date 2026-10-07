package com.example.quakeapplication.fakes


import com.example.quakeapplication.domain.model.Earthquake
import java.time.Instant

// Builds a test earthquake where only the fields a test cares about need to be given
fun testEarthquake(
    id: String,
    magnitude: Double? = 1.0,
    timeMillis: Long = 0L,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
) = Earthquake(
    id = id,
    magnitude = magnitude,
    place = null,
    time = Instant.ofEpochMilli(timeMillis),
    latitude = latitude,
    longitude = longitude,
    depthKm = 0.0,
    hasTsunamiFlag = false,
    detailUrl = null,
)
