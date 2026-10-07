package com.example.quakeapplication.data

import com.example.quakeapplication.data.remote.EarthquakeDto
import retrofit2.http.GET

interface EarthquakeApi {
    @GET(value = "all_day.geojson")
    suspend fun fetchEarthquakesPerDay(): EarthquakeDto
}