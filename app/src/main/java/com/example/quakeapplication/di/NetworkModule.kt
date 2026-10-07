package com.example.quakeapplication.di

import com.example.quakeapplication.data.EarthquakeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Recipes telling Hilt how to build networking objects it cannot build on its own
// SingletonComponent: these live as long as the app process
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // The GeoJSON summary feeds live under this path; the API interface appends all_day.geojson etc.
    private const val USGS_BASE_URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // The feed has about 25 fields per earthquake and our DTO lists 5; unknown fields must be skipped
        ignoreUnknownKeys = true
    }

    // One shared client: each client owns a thread pool and connection pool,
    // and sharing lets requests reuse open connections instead of repeating the TLS handshake
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        // A hard limit on the whole call, so a stuck network gives up and the cached data stays on screen
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideUsgsApi(client: OkHttpClient, json: Json): EarthquakeApi =
        Retrofit.Builder()
            .baseUrl(USGS_BASE_URL)
            .client(client)
            // Read response bodies with kotlinx.serialization, which uses generated code, not reflection
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            // Retrofit writes the class behind the UsgsApi interface here
            .create(EarthquakeApi::class.java)
}