package com.example.quakeapplication.domain.model

sealed interface RefreshResult {
    // New data was downloaded and saved; screens see it because they watch the database
    data object Success :RefreshResult

    // Nothing was saved, so the old data stays on screen
    data class Failure(val error: DataError) : RefreshResult
}