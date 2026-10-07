package com.example.quakeapplication.domain.repository

import com.example.quakeapplication.domain.model.Earthquake
import com.example.quakeapplication.domain.model.RefreshResult
import kotlinx.coroutines.flow.Flow

// The contract the rest of the app depends on: WHAT can be done, never HOW
// The implementation lives in the data layer; tests can swap in a fake
interface EarthquakeRepository {
    // Sends the stored list now, and again whenever it changes
    fun observeEarthquakes(): Flow<List<Earthquake>>

    // One earthquake; null means it is no longer stored
    fun observeEarthquake(id: String): Flow<Earthquake?>

    // Downloads the latest data and saves it; returns a Failure instead of throwing
    suspend fun refresh(): RefreshResult
}