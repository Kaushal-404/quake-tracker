package com.example.quakeapplication.data.mapper

import com.example.quakeapplication.data.local.EarthquakeEntity
import com.example.quakeapplication.data.remote.EarthquakeDto
import com.example.quakeapplication.data.remote.FeatureDto
import com.example.quakeapplication.domain.model.Earthquake
import java.time.Instant

// The whole download as database rows; earthquakes too broken to keep are skipped
fun EarthquakeDto.toEntities(): List<EarthquakeEntity> =
    features.orEmpty().mapNotNull { feature -> feature.toEntityOrNull() }

// The ONE place where untrusted server data becomes trusted app data
// Returns null when a field we cannot work without is missing
fun FeatureDto.toEntityOrNull(): EarthquakeEntity? {
    // No id: cannot store it, update it, or open its detail screen
    val safeId = id ?: return null
    // No time: cannot sort it or say when it happened
    val safeTime = properties?.time ?: return null
    // Coordinates are longitude, latitude, depth, in that order
    val coordinates = geometry?.coordinates ?: return null
    // getOrNull returns null instead of crashing when the list is shorter than expected
    val safeLongitude = coordinates.getOrNull(0) ?: return null
    val safeLatitude = coordinates.getOrNull(1) ?: return null
    // Better to drop a broken row than to invent a depth
    val safeDepth = coordinates.getOrNull(2) ?: return null

    return EarthquakeEntity(
        id = safeId,
        // Missing magnitude and place are real states; the screen decides how to show them
        magnitude = properties.mag,
        place = properties.place,
        timeEpochMillis = safeTime,
        latitude = safeLatitude,
        longitude = safeLongitude,
        depthKm = safeDepth,
        // Only an explicit 1 means flagged; missing means no flag
        hasTsunamiFlag = properties.tsunami == 1,
        detailUrl = properties.url,
    )
}

// A stored row is already valid, so this cannot fail
fun EarthquakeEntity.toDomain(): Earthquake = Earthquake(
    id = id,
    magnitude = magnitude,
    place = place,
    // The one real conversion: a raw number becomes a proper point in time
    time = Instant.ofEpochMilli(timeEpochMillis),
    latitude = latitude,
    longitude = longitude,
    depthKm = depthKm,
    hasTsunamiFlag = hasTsunamiFlag,
    detailUrl = detailUrl,
)
