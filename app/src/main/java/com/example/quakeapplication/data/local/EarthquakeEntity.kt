package com.example.quakeapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
// One row in the "earthquakes" table; Room writes CREATE TABLE from this class at build time
// Kept separate from the domain model so a database change never forces a UI change
@Entity(tableName = "earthquakes")
data class EarthquakeEntity(
    // The USGS id is the key, so each earthquake is stored once
    @PrimaryKey val id: String,
    val magnitude: Double?,
    val place: String?,
    // SQLite has no date type; a number lets the database sort by time directly
    val timeEpochMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val depthKm: Double,
    val hasTsunamiFlag: Boolean,
    val detailUrl: String?,
)
