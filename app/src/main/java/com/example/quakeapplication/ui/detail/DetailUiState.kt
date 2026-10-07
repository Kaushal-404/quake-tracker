package com.example.quakeapplication.ui.detail

import com.example.quakeapplication.domain.model.Earthquake


// The detail screen is in exactly one of these states
sealed interface DetailUiState {
    data object Loading : DetailUiState
    // The earthquake left the 24-hour window and was removed by a refresh
    data object NotFound : DetailUiState
    data class Loaded(val earthquake: Earthquake, val distanceKm: Double?) : DetailUiState
}
