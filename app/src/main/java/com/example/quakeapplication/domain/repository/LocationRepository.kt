package com.example.quakeapplication.domain.repository

import com.example.quakeapplication.domain.model.UserLocation

// Where the user is, without any Android types leaking into the domain layer

interface LocationRepository {
    // True when the user has allowed location access
    fun hasPermission(): Boolean

    // The user's approximate position, or null if not allowed, switched off, or unavailable
    suspend fun getCurrentLocation(): UserLocation?
}