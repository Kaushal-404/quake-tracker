package com.example.quakeapplication.fakes

import com.example.quakeapplication.domain.model.UserLocation
import com.example.quakeapplication.domain.repository.LocationRepository


class FakeLocationRepository(
    var permissionGranted: Boolean = false,
    var location: UserLocation? = null,
) : LocationRepository {
    override fun hasPermission(): Boolean = permissionGranted
    override suspend fun getCurrentLocation(): UserLocation? = if (permissionGranted) location else null
}
