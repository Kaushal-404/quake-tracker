package com.example.quakeapplication.domain.model

// The daily feed is mostly tiny events; a minimum magnitude lets the user see only the ones that matter
enum class MagnitudeFilter(val minimumMagnitude: Double?) {
    ALL(null),
    // Roughly the point at which an earthquake is felt by people nearby
    MODERATE(2.5),
    // Roughly the point at which an earthquake can cause damage
    STRONG(4.5),
    ;

    // An unknown magnitude only passes the "all" filter: we cannot claim it is above a threshold
    fun matches(magnitude: Double?): Boolean =
        minimumMagnitude == null || (magnitude != null && magnitude >= minimumMagnitude)
}
