package com.example.quakeapplication.domain.usecase

import com.example.quakeapplication.domain.geo.distanceFrom
import com.example.quakeapplication.domain.model.Earthquake
import com.example.quakeapplication.domain.model.EarthquakeWithDistance
import com.example.quakeapplication.domain.model.MagnitudeFilter
import com.example.quakeapplication.domain.model.SortOrder
import com.example.quakeapplication.domain.model.UserLocation
import com.example.quakeapplication.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveEarthquakesUseCase @Inject constructor(
    private val repository: EarthquakeRepository,
) {
    // "operator fun invoke" lets callers use this object like a function
    // Inputs are streams, so the result updates when the data, the sort, the filter OR the location changes
    operator fun invoke(
        sortOrder: Flow<SortOrder>,
        magnitudeFilter: Flow<MagnitudeFilter>,
        userLocation: Flow<UserLocation?>,
    ): Flow<List<EarthquakeWithDistance>> =
        // combine waits for each stream's latest value, then re-runs the block whenever any of them changes
        combine(repository.observeEarthquakes(), sortOrder, magnitudeFilter, userLocation) { earthquakes, sort, filter, location ->
            earthquakes
                .filter { earthquake -> filter.matches(earthquake.magnitude) }
                .map { earthquake -> earthquake.withDistanceFrom(location) }
                .sortedFor(sort)
        }
}

// Distance is only known when the location is known
private fun Earthquake.withDistanceFrom(location: UserLocation?) = EarthquakeWithDistance(
    earthquake = this,
    distanceKm = location?.let { distanceFrom(it) },
)

private fun List<EarthquakeWithDistance>.sortedFor(sortOrder: SortOrder): List<EarthquakeWithDistance> =
    when (sortOrder) {
        SortOrder.NEWEST -> sortedByDescending { item -> item.earthquake.time }
        // An unknown magnitude counts as the lowest possible, so those go to the end
        SortOrder.STRONGEST -> sortedByDescending { item ->
            item.earthquake.magnitude ?: Double.NEGATIVE_INFINITY
        }
        // Unknown distances go last; Kotlin's sort is stable, so with no location the input order is kept
        SortOrder.NEAREST -> sortedWith(compareBy(nullsLast()) { item -> item.distanceKm })
    }
