package com.example.quakeapplication.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

// Coarse (city-level) only: enough for distances, and less to ask of the user than precise location
const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION

fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED

// Returns a function that shows the system permission dialog
// The result arrives in onResult; the launcher survives rotation because Compose remembers it
@Composable
fun rememberLocationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return { launcher.launch(LOCATION_PERMISSION) }
}
