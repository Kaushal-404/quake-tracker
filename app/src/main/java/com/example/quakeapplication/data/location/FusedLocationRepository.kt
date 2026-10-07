package com.example.quakeapplication.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.quakeapplication.domain.model.UserLocation
import com.example.quakeapplication.domain.repository.LocationRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton


private const val LOCATION_TIMEOUT_MS = 10_000L

// Gets the user's position from Google Play Services' fused provider,
// which combines Wi-Fi, cell towers and GPS and picks the cheapest that is accurate enough
@Singleton
class FusedLocationRepository  @Inject constructor(
    // The application's context, never an activity's, so this long-lived object cannot leak a screen
    @param:ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient,
) : LocationRepository {
    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    // The lint warning is suppressed because we check the permission on the first line
    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCurrentLocation(): UserLocation? {
        if (!hasPermission()) return null
        return try {
            withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                // Lets us cancel the Play Services request if the coroutine is cancelled
                val cancellation = CancellationTokenSource()
                // Balanced power: city-level accuracy from Wi-Fi and cell towers, usually without GPS
                val fresh = client
                    .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
                    .await(cancellation)
                // A fresh fix can be null (for example, location switched off); fall back to the last known one
                val location = fresh ?: client.lastLocation.await()
                location?.let { UserLocation(it.latitude, it.longitude) }
            }
        } catch (error: SecurityException) {
            // The user revoked permission between our check and the call
            null
        } catch (error: ApiException) {
            // Google Play Services is missing or out of date on this device
            null
        }
    }
}