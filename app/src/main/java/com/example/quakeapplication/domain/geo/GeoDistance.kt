package com.example.quakeapplication.domain.geo

import com.example.quakeapplication.domain.model.Earthquake
import com.example.quakeapplication.domain.model.UserLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// The Earth's average radius; the source of the small error in this formula
private const val EARTH_RADIUS_KM = 6371.0

// Straight-line distance over the Earth's surface between two points, using the haversine formula
// It treats the Earth as a perfect sphere, which is accurate to about half a percent
fun kilometersBetween(
    fromLatitude: Double,
    fromLongitude: Double,
    toLatitude: Double,
    toLongitude: Double,
): Double {
    // The formula works in radians, not degrees
    val deltaLatitude = Math.toRadians(toLatitude - fromLatitude)
    val deltaLongitude = Math.toRadians(toLongitude - fromLongitude)
    val fromLatitudeRadians = Math.toRadians(fromLatitude)
    val toLatitudeRadians = Math.toRadians(toLatitude)

    // "a" is the square of half the straight chord length between the points, on a unit sphere
    val a = sin(deltaLatitude / 2) * sin(deltaLatitude / 2) +
            cos(fromLatitudeRadians) * cos(toLatitudeRadians) *
            sin(deltaLongitude / 2) * sin(deltaLongitude / 2)
    // "c" is the angle between the two points, seen from the Earth's centre
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    // Arc length equals radius times angle
    return EARTH_RADIUS_KM * c
}

// Convenience version used by the use case and the detail screen
fun Earthquake.distanceFrom(location: UserLocation): Double =
    kilometersBetween(location.latitude, location.longitude, latitude, longitude)
