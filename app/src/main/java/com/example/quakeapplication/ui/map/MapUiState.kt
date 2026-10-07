package com.example.quakeapplication.ui.map

import com.example.quakeapplication.domain.model.DataError
import com.example.quakeapplication.domain.model.Earthquake


// Everything the map screen draws
data class MapUiState(
    val earthquakes: List<Earthquake> = emptyList(),
    val isRefreshing: Boolean = false,
    val refreshError: DataError? = null,
)
