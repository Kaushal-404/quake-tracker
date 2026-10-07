package com.example.quakeapplication.ui.common

import android.text.format.DateUtils
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// Turning data into display text belongs in the UI layer, not in the domain model

// One decimal place, which is how magnitudes are usually reported; a dash when unknown
fun formatMagnitude(magnitude: Double?): String =
    magnitude?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "–"

// Whole kilometres with thousands separators, for example "1,234"
fun formatKilometers(kilometers: Double): String =
    String.format(Locale.getDefault(), "%,.0f", kilometers)

// Short relative time such as "12 minutes ago", in the user's language
fun formatRelativeTime(time: Instant, now: Long = System.currentTimeMillis()): String =
    DateUtils.getRelativeTimeSpanString(time.toEpochMilli(), now, DateUtils.MINUTE_IN_MILLIS).toString()

// Full date and time in the phone's own time zone and format
fun formatFullTime(time: Instant): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
        .format(time)

// Four decimal places is about 11 metres, more than enough
fun formatCoordinates(latitude: Double, longitude: Double): String =
    String.format(Locale.getDefault(), "%.4f, %.4f", latitude, longitude)

// Colour by strength, shared by the list badge and the map markers so both read the same way
fun magnitudeColor(magnitude: Double?): Color = when {
    magnitude == null -> Color(0xFF9E9E9E)
    magnitude < 3.0 -> Color(0xFF43A047)
    magnitude < 5.0 -> Color(0xFFFB8C00)
    else -> Color(0xFFE53935)
}
