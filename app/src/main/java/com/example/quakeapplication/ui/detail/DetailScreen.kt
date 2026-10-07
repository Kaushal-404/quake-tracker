package com.example.quakeapplication.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quakeapplication.R
import com.example.quakeapplication.domain.model.Earthquake
import com.example.quakeapplication.ui.common.MagnitudeBadge
import com.example.quakeapplication.ui.common.formatCoordinates
import com.example.quakeapplication.ui.common.formatFullTime
import com.example.quakeapplication.ui.common.formatKilometers
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun DetailRoute(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(state: DetailUiState, onBack: () -> Unit) {
    // No bottom bar on this screen, so it keeps clear of the system navigation bar itself
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        TopAppBar(
            title = { Text(stringResource(R.string.detail_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
        )
        when (state) {
            DetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            DetailUiState.NotFound -> Text(
                text = stringResource(R.string.detail_not_found),
                modifier = Modifier.padding(16.dp),
            )
            is DetailUiState.Loaded -> DetailContent(earthquake = state.earthquake, distanceKm = state.distanceKm)
        }
    }
}

@Composable
private fun DetailContent(earthquake: Earthquake, distanceKm: Double?) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        EarthquakeLocationMap(earthquake)

        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            MagnitudeBadge(magnitude = earthquake.magnitude, size = 64.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = earthquake.place ?: stringResource(R.string.unknown_place),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        HorizontalDivider()
        DetailRow(stringResource(R.string.detail_time), formatFullTime(earthquake.time))
        DetailRow(stringResource(R.string.detail_depth), stringResource(R.string.detail_depth_value, formatKilometers(earthquake.depthKm)))
        DetailRow(stringResource(R.string.detail_coordinates), formatCoordinates(earthquake.latitude, earthquake.longitude))
        distanceKm?.let { distance ->
            DetailRow(stringResource(R.string.detail_distance), stringResource(R.string.distance_away, formatKilometers(distance)))
        }
        DetailRow(
            stringResource(R.string.detail_tsunami),
            stringResource(if (earthquake.hasTsunamiFlag) R.string.detail_tsunami_yes else R.string.detail_tsunami_no),
        )

        earthquake.detailUrl?.let { url ->
            Button(
                onClick = { uriHandler.openUri(url) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text(stringResource(R.string.detail_open_usgs))
            }
        }
    }
}

// A small map centred on the earthquake
@Composable
private fun EarthquakeLocationMap(earthquake: Earthquake) {
    val earthquakePosition = LatLng(earthquake.latitude, earthquake.longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(earthquakePosition, 5f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
    ) {
        Marker(state = rememberUpdatedMarkerState(position = earthquakePosition))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
