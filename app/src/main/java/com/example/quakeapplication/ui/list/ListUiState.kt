package com.example.quakeapplication.ui.list

import com.example.quakeapplication.domain.model.DataError
import com.example.quakeapplication.domain.model.EarthquakeWithDistance
import com.example.quakeapplication.domain.model.MagnitudeFilter
import com.example.quakeapplication.domain.model.SortOrder
import com.example.quakeapplication.ui.common.LocationState

// Everything the list screen draws, in one immutable object
// The screen is a pure function of this: same state in, same pixels out
data class ListUiState(
    // True only until the first read from the database arrives
    val isLoading: Boolean = true,
    val earthquakes: List<EarthquakeWithDistance> = emptyList(),
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val magnitudeFilter: MagnitudeFilter = MagnitudeFilter.ALL,
    val locationState: LocationState = LocationState.Checking,
    val isRefreshing: Boolean = false,
    // The last refresh failure, shown as a banner over the saved data; null means no banner
    val refreshError: DataError? = null,
)
