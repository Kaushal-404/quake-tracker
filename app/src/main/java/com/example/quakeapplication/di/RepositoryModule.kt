package com.example.quakeapplication.di

import com.example.quakeapplication.data.location.FusedLocationRepository
import com.example.quakeapplication.data.repository.OfflineFirstEarthquakeRepository
import com.example.quakeapplication.domain.repository.EarthquakeRepository
import com.example.quakeapplication.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// The only place that knows which implementation stands behind each domain interface
// @Binds generates no extra runtime code, unlike @Provides, so it is the cheaper choice for interfaces
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindEarthquakeRepository(
        implementation: OfflineFirstEarthquakeRepository,
    ): EarthquakeRepository

    @Binds
    abstract fun bindLocationRepository(
        implementation: FusedLocationRepository,
    ): LocationRepository
}