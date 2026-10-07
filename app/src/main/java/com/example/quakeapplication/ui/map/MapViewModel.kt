package com.example.quakeapplication.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quakeapplication.domain.model.DataError
import com.example.quakeapplication.domain.model.RefreshResult
import com.example.quakeapplication.domain.repository.EarthquakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// The map needs every stored earthquake with no sorting or distance,
// so it reads the repository directly; a use case here would only forward the call
@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: EarthquakeRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<DataError?>(null)

    val uiState: StateFlow<MapUiState> = combine(
        repository.observeEarthquakes(),
        isRefreshing,
        refreshError,
    ) { earthquakes, refreshing, error ->
        MapUiState(earthquakes = earthquakes, isRefreshing = refreshing, refreshError = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    // No refresh on start: the list screen already refreshed when the app opened
    fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                refreshError.value = when (val result = repository.refresh()) {
                    RefreshResult.Success -> null
                    is RefreshResult.Failure -> result.error
                }
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun onErrorDismissed() {
        refreshError.value = null
    }
}
