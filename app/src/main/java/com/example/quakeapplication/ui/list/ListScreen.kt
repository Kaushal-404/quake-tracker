package com.example.quakeapplication.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.quakeapplication.R
import com.example.quakeapplication.domain.model.EarthquakeWithDistance
import com.example.quakeapplication.domain.model.MagnitudeFilter
import com.example.quakeapplication.domain.model.SortOrder
import com.example.quakeapplication.ui.common.ErrorBanner
import com.example.quakeapplication.ui.common.LocationState
import com.example.quakeapplication.ui.common.MagnitudeBadge
import com.example.quakeapplication.ui.common.formatKilometers
import com.example.quakeapplication.ui.common.formatRelativeTime
import com.example.quakeapplication.ui.common.rememberLocationPermissionRequest

// The "route" composable: connects the ViewModel to the screen
// Keeping the screen below free of the ViewModel makes it easy to preview and test
@Composable
fun ListRoute(
    onEarthquakeClick: (String) -> Unit,
    viewModel: ListViewModel = hiltViewModel(),
) {
    // Stops collecting when the app goes to the background, so no work runs off-screen
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val requestLocationPermission = rememberLocationPermissionRequest(viewModel::onLocationPermissionResult)

    ListScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onSortSelected = viewModel::onSortSelected,
        onMagnitudeFilterSelected = viewModel::onMagnitudeFilterSelected,
        onErrorDismissed = viewModel::onErrorDismissed,
        onAllowLocation = requestLocationPermission,
        onLocationPromptDismissed = viewModel::onLocationPromptDismissed,
        onEarthquakeClick = onEarthquakeClick,
    )
}

// Stateless: draws the state and reports user actions upward
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    state: ListUiState,
    onRefresh: () -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onMagnitudeFilterSelected: (MagnitudeFilter) -> Unit,
    onErrorDismissed: () -> Unit,
    onAllowLocation: () -> Unit,
    onLocationPromptDismissed: () -> Unit,
    onEarthquakeClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.list_title)) })

        if (state.locationState == LocationState.PermissionNeeded) {
            LocationPrompt(onAllow = onAllowLocation, onDismiss = onLocationPromptDismissed)
        }

        SortChips(
            selected = state.sortOrder,
            // "Nearest" only makes sense when we know where the user is
            isNearestEnabled = state.locationState is LocationState.Available,
            onSortSelected = onSortSelected,
        )
        MagnitudeChips(selected = state.magnitudeFilter, onFilterSelected = onMagnitudeFilterSelected)

        state.refreshError?.let { error ->
            ErrorBanner(error = error, onRetry = onRefresh, onDismiss = onErrorDismissed)
        }

        // Pulling down past the top of the list triggers onRefresh
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                // Nothing to show yet and something is loading: a spinner, not an empty message
                state.isLoading || (state.earthquakes.isEmpty() && state.isRefreshing) -> CenteredProgress()
                state.earthquakes.isEmpty() -> EmptyState()
                else -> EarthquakeList(items = state.earthquakes, onEarthquakeClick = onEarthquakeClick)
            }
        }
    }
}

@Composable
private fun EarthquakeList(items: List<EarthquakeWithDistance>, onEarthquakeClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // A stable key per earthquake lets Compose keep and move rows instead of redrawing all of them
        items(items = items, key = { item -> item.earthquake.id }) { item ->
            EarthquakeRow(item = item, onClick = { onEarthquakeClick(item.earthquake.id) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun EarthquakeRow(item: EarthquakeWithDistance, onClick: () -> Unit) {
    val earthquake = item.earthquake
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MagnitudeBadge(magnitude = earthquake.magnitude)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = earthquake.place ?: stringResource(R.string.unknown_place),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatRelativeTime(earthquake.time),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Only shown when we know where the user is
            item.distanceKm?.let { distance ->
                Text(
                    text = stringResource(R.string.distance_away, formatKilometers(distance)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SortChips(selected: SortOrder, isNearestEnabled: Boolean, onSortSelected: (SortOrder) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SortOrder.entries.forEach { order ->
            FilterChip(
                selected = order == selected,
                onClick = { onSortSelected(order) },
                enabled = order != SortOrder.NEAREST || isNearestEnabled,
                label = { Text(stringResource(order.labelRes())) },
            )
        }
    }
}

private fun SortOrder.labelRes(): Int = when (this) {
    SortOrder.NEWEST -> R.string.sort_newest
    SortOrder.STRONGEST -> R.string.sort_strongest
    SortOrder.NEAREST -> R.string.sort_nearest
}

@Composable
private fun MagnitudeChips(selected: MagnitudeFilter, onFilterSelected: (MagnitudeFilter) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MagnitudeFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onFilterSelected(filter) },
                label = { Text(stringResource(filter.labelRes())) },
            )
        }
    }
}

private fun MagnitudeFilter.labelRes(): Int = when (this) {
    MagnitudeFilter.ALL -> R.string.filter_all
    MagnitudeFilter.MODERATE -> R.string.filter_moderate
    MagnitudeFilter.STRONG -> R.string.filter_strong
}

@Composable
private fun LocationPrompt(onAllow: () -> Unit, onDismiss: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.location_prompt_body), style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.location_prompt_dismiss)) }
                TextButton(onClick = onAllow) { Text(stringResource(R.string.location_prompt_allow)) }
            }
        }
    }
}

@Composable
private fun CenteredProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    // Scrollable even though it is short: pull-to-refresh only works on scrollable content
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.empty_body), style = MaterialTheme.typography.bodyMedium)
    }
}
