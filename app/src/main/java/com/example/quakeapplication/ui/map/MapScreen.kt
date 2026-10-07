package com.example.quakeapplication.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quakeapplication.R
import com.example.quakeapplication.domain.model.Earthquake
import com.example.quakeapplication.ui.common.ErrorBanner
import com.example.quakeapplication.ui.common.MagnitudeBadge
import com.example.quakeapplication.ui.common.formatMagnitude
import com.example.quakeapplication.ui.common.hasLocationPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun MapRoute(
    onEarthquakeClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MapScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onErrorDismissed = viewModel::onErrorDismissed,
        onEarthquakeClick = onEarthquakeClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    state: MapUiState,
    onRefresh: () -> Unit,
    onErrorDismissed: () -> Unit,
    onEarthquakeClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.map_title)) },
            actions = {
                // No pull-to-refresh here, because dragging the map would trigger it; a button instead
                if (state.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(24.dp))
                } else {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                }
            },
        )
        state.refreshError?.let { error ->
            ErrorBanner(error = error, onRetry = onRefresh, onDismiss = onErrorDismissed)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            EarthquakeMap(earthquakes = state.earthquakes, onEarthquakeClick = onEarthquakeClick)
        }
    }
}

// The map library's view of one earthquake. A private adapter, so the domain model never depends on map types
private class EarthquakeClusterItem(
    val earthquake: Earthquake,
    private val title: String,
    private val snippet: String,
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(earthquake.latitude, earthquake.longitude)
    override fun getTitle(): String = title
    override fun getSnippet(): String = snippet
    override fun getZIndex(): Float? = null
}

// Padding, in pixels, kept between a zoomed-in cluster's pins and the edge of the map
private const val CLUSTER_ZOOM_PADDING_PX = 120

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun EarthquakeMap(earthquakes: List<Earthquake>, onEarthquakeClick: (String) -> Unit) {
    // Starts zoomed out over the whole world; remembered so it survives recomposition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 1f)
    }
    val scope = rememberCoroutineScope()
    // The blue "you are here" dot needs location permission, otherwise the map SDK throws
    val showMyLocation = LocalContext.current.hasLocationPermission()
    // Text is read once here, outside the map's own composition
    val unknownPlace = stringResource(R.string.unknown_place)
    val markerSnippet = stringResource(R.string.map_marker_snippet)
    // Rebuilt only when the list changes, not on every recomposition
    val items = remember(earthquakes, unknownPlace, markerSnippet) {
        earthquakes.map { earthquake ->
            EarthquakeClusterItem(
                earthquake = earthquake,
                title = "M ${formatMagnitude(earthquake.magnitude)} · ${earthquake.place ?: unknownPlace}",
                snippet = markerSnippet,
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = showMyLocation),
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
    ) {
        // Nearby pins merge into one numbered bubble when zoomed out, so a busy region stays readable
        Clustering(
            items = items,
            // Tapping a bubble zooms in until its pins separate
            onClusterClick = { cluster ->
                scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(cluster.bounds(), CLUSTER_ZOOM_PADDING_PX)) }
                true
            },
            // Tapping a pin opens its title; tapping the title opens the detail screen
            onClusterItemInfoWindowClick = { item -> onEarthquakeClick(item.earthquake.id) },
            // Each pin is the same badge as the list, so both read the same way
            clusterItemContent = { item -> MagnitudeBadge(magnitude = item.earthquake.magnitude, size = 36.dp) },
        )
    }
}

// The smallest rectangle that contains every pin in the cluster
private fun Cluster<EarthquakeClusterItem>.bounds(): LatLngBounds =
    LatLngBounds.builder().also { builder -> items.forEach { item -> builder.include(item.position) } }.build()
