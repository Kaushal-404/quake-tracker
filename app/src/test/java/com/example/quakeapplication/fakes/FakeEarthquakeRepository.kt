package com.example.quakeapplication.fakes

import com.example.quakeapplication.domain.model.Earthquake
import com.example.quakeapplication.domain.model.RefreshResult
import com.example.quakeapplication.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// A repository with no network or database: the test sets the stored list and the next refresh result
class FakeEarthquakeRepository(initial: List<Earthquake> = emptyList()) : EarthquakeRepository {

    val stored = MutableStateFlow(initial)
    var nextRefreshResult: RefreshResult = RefreshResult.Success
    var refreshCount = 0
        private set

    override fun observeEarthquakes(): Flow<List<Earthquake>> = stored

    override fun observeEarthquake(id: String): Flow<Earthquake?> =
        stored.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun refresh(): RefreshResult {
        refreshCount++
        return nextRefreshResult
    }
}
