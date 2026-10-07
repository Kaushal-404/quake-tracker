package com.example.quakeapplication.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quakeapplication.domain.model.DataError
import com.example.quakeapplication.domain.model.MagnitudeFilter
import com.example.quakeapplication.domain.model.RefreshResult
import com.example.quakeapplication.domain.model.SortOrder
import com.example.quakeapplication.domain.repository.EarthquakeRepository
import com.example.quakeapplication.domain.repository.LocationRepository
import com.example.quakeapplication.domain.usecase.ObserveEarthquakesUseCase
import com.example.quakeapplication.ui.common.LocationState
import com.example.quakeapplication.ui.common.locationOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Holds the list screen's state and survives rotation
// It never touches Retrofit, Room or Android location APIs: only domain interfaces
@HiltViewModel
class ListViewModel @Inject constructor(
    observeEarthquakes: ObserveEarthquakesUseCase,
    private val repository: EarthquakeRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    // The spinner and the error banner always change together, so they live in one value
    private data class RefreshState(val isRefreshing: Boolean = false, val error: DataError? = null)

    // Private, writable pieces of state; only this class can change them
    private val sortOrder = MutableStateFlow(SortOrder.NEWEST)
    private val magnitudeFilter = MutableStateFlow(MagnitudeFilter.ALL)
    private val locationState = MutableStateFlow<LocationState>(LocationState.Checking)
    private val refreshState = MutableStateFlow(RefreshState())

    // The one public, read-only state the screen observes
    val uiState: StateFlow<ListUiState> = combine(
        // The use case re-filters and re-sorts whenever the data, the choices or the location change
        observeEarthquakes(sortOrder, magnitudeFilter, locationState.map { it.locationOrNull }),
        sortOrder,
        magnitudeFilter,
        locationState,
        refreshState,
    ) { earthquakes, sort, filter, location, refresh ->
        ListUiState(
            isLoading = false,
            earthquakes = earthquakes,
            sortOrder = sort,
            magnitudeFilter = filter,
            locationState = location,
            isRefreshing = refresh.isRefreshing,
            refreshError = refresh.error,
        )
    }.stateIn(
        scope = viewModelScope,
        // Keeps the database query alive for 5 seconds after the screen stops watching,
        // so a rotation (which re-subscribes within milliseconds) does not restart it,
        // but leaving the app does stop it and saves battery
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListUiState(),
    )

    init {
        // Cached data shows at once from the database; this fetches fresh data in the background
        refresh()
        loadLocation()
    }

    fun refresh() {
        // Ignore a second pull while one is already running; safe without a lock because this runs on the main thread
        if (refreshState.value.isRefreshing) return
        viewModelScope.launch {
            refreshState.update { it.copy(isRefreshing = true) }
            try {
                val error = when (val result = repository.refresh()) {
                    RefreshResult.Success -> null
                    is RefreshResult.Failure -> result.error
                }
                refreshState.update { it.copy(error = error) }
            } finally {
                // "finally" also runs if the coroutine is cancelled, so the spinner can never get stuck
                refreshState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onSortSelected(order: SortOrder) {
        sortOrder.value = order
    }

    fun onMagnitudeFilterSelected(filter: MagnitudeFilter) {
        magnitudeFilter.value = filter
    }

    fun onErrorDismissed() {
        refreshState.update { it.copy(error = null) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) loadLocation() else locationState.value = LocationState.Declined
    }

    fun onLocationPromptDismissed() {
        locationState.value = LocationState.Declined
    }

    private fun loadLocation() {
        if (!locationRepository.hasPermission()) {
            // Do not re-show the prompt if the user already declined in this session
            if (locationState.value != LocationState.Declined) {
                locationState.value = LocationState.PermissionNeeded
            }
            return
        }
        viewModelScope.launch {
            locationState.value = LocationState.Checking
            val location = locationRepository.getCurrentLocation()
            locationState.value = if (location != null) LocationState.Available(location) else LocationState.Unavailable
        }
    }
}
