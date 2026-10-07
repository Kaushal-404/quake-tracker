package com.example.quakeapplication.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.quakeapplication.domain.geo.distanceFrom
import com.example.quakeapplication.domain.model.UserLocation
import com.example.quakeapplication.domain.repository.EarthquakeRepository
import com.example.quakeapplication.domain.repository.LocationRepository
import com.example.quakeapplication.ui.navigation.DetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    // Holds the navigation arguments, and is saved by the system if the process is killed
    savedStateHandle: SavedStateHandle,
    repository: EarthquakeRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    // Reads the id that was passed in DetailDestination(earthquakeId)
    private val earthquakeId = savedStateHandle.toRoute<DetailDestination>().earthquakeId
    private val userLocation = MutableStateFlow<UserLocation?>(null)

    // Watching the database (not taking a one-time copy) means a refresh updates this screen too
    val uiState: StateFlow<DetailUiState> = combine(
        repository.observeEarthquake(earthquakeId),
        userLocation,
    ) { earthquake, location ->
        if (earthquake == null) {
            DetailUiState.NotFound
        } else {
            DetailUiState.Loaded(earthquake = earthquake, distanceKm = location?.let { earthquake.distanceFrom(it) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

    init {
        // Returns null without asking when permission was not given; the distance row is then hidden
        viewModelScope.launch {
            userLocation.value = locationRepository.getCurrentLocation()
        }
    }
}
