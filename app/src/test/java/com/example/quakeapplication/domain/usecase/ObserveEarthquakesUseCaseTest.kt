package com.example.quakeapplication.domain.usecase

import com.example.quakeapplication.domain.model.MagnitudeFilter
import com.example.quakeapplication.domain.model.SortOrder
import com.example.quakeapplication.domain.model.UserLocation
import com.example.quakeapplication.fakes.FakeEarthquakeRepository
import com.example.quakeapplication.fakes.testEarthquake
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObserveEarthquakesUseCaseTest {

    // a: newest, weak, far    b: oldest, strongest, near    c: middle, unknown magnitude, middle distance
    private val a = testEarthquake(id = "a", magnitude = 2.0, timeMillis = 300, latitude = 0.0, longitude = 90.0)
    private val b = testEarthquake(id = "b", magnitude = 5.0, timeMillis = 100, latitude = 0.0, longitude = 1.0)
    private val c = testEarthquake(id = "c", magnitude = null, timeMillis = 200, latitude = 0.0, longitude = 30.0)

    private val useCase = ObserveEarthquakesUseCase(FakeEarthquakeRepository(listOf(b, c, a)))
    private val user = UserLocation(latitude = 0.0, longitude = 0.0)

    private suspend fun idsFor(
        order: SortOrder,
        location: UserLocation?,
        filter: MagnitudeFilter = MagnitudeFilter.ALL,
    ) = useCase(flowOf(order), flowOf(filter), flowOf(location)).first().map { it.earthquake.id }

    @Test
    fun newest_ordersByTimeDescending() = runTest {
        assertEquals(listOf("a", "c", "b"), idsFor(SortOrder.NEWEST, null))
    }

    @Test
    fun strongest_putsUnknownMagnitudeLast() = runTest {
        assertEquals(listOf("b", "a", "c"), idsFor(SortOrder.STRONGEST, null))
    }

    @Test
    fun nearest_ordersByDistanceFromTheUser() = runTest {
        assertEquals(listOf("b", "c", "a"), idsFor(SortOrder.NEAREST, user))
    }

    @Test
    fun withoutLocation_distanceIsNull_andNearestKeepsTheStoredOrder() = runTest {
        val items = useCase(flowOf(SortOrder.NEAREST), flowOf(MagnitudeFilter.ALL), flowOf(null)).first()
        assertNull(items.first().distanceKm)
        assertEquals(listOf("b", "c", "a"), items.map { it.earthquake.id })
    }

    @Test
    fun moderateFilter_dropsWeakAndUnknownMagnitudes() = runTest {
        // a is M2.0 (below 2.5) and c has no magnitude; only b (M5.0) remains
        assertEquals(listOf("b"), idsFor(SortOrder.NEWEST, null, MagnitudeFilter.MODERATE))
    }

    @Test
    fun strongFilter_keepsOnlyDamagingEarthquakes() = runTest {
        assertEquals(listOf("b"), idsFor(SortOrder.NEWEST, null, MagnitudeFilter.STRONG))
    }
}
