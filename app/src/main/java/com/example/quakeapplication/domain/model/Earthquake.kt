package com.example.quakeapplication.domain.model

import java.time.Instant

data class Earthquake(
    // The USGS event id, for example "nc75012345"
    // It is our unique key for the database, list rows and navigation
    val id: String,
    // Nullable on purpose: USGS sometimes publishes an event before the magnitude is calculated
    // We keep that truth here and let the screen decide how to show "unknown"
    val magnitude: Double?,
    // Free text from USGS, for example "10 km NE of Ridgecrest, CA"
    // Nullable because USGS does not always send it
    val place: String?,
    // The exact moment the earthquake happened
    // Instant is one point in time with no time zone, so it cannot be misread
    // The screen converts it to the user's local time when drawing
    val time: Instant,
    // Where it happened on the map
    val latitude: Double,
    val longitude: Double,
    // How deep under the ground it started
    // The unit is in the name so nobody has to guess between kilometres and miles
    val depthKm: Double,
    // True when USGS flagged this event for possible tsunami risk
    val hasTsunamiFlag: Boolean,
    // Link to the USGS web page for this event, shown on the detail screen
    val detailUrl: String?,
)
