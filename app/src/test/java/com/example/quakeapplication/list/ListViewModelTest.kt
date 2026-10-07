package com.example.quakeapplication.list

import com.example.quakeapplication.domain.model.DataError
import com.example.quakeapplication.domain.model.MagnitudeFilter
import com.example.quakeapplication.domain.model.RefreshResult
import com.example.quakeapplication.domain.model.SortOrder
import com.example.quakeapplication.domain.model.UserLocation
import com.example.quakeapplication.fakes.FakeEarthquakeRepository
import com.example.quakeapplication.fakes.FakeLocationRepository
import com.example.quakeapplication.fakes.MainDispatcherRule
import com.example.quakeapplication.fakes.testEarthquake
import com.example.quakeapplication.ui.common.LocationState
import com.example.quakeapplication.domain.usecase.ObserveEarthquakesUseCase
import com.example.quakeapplication.ui.list.ListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeEarthquakeRepository(listOf(testEarthquake("a")))
    private val locationRepository = FakeLocationRepository()

    private fun createViewModel() =
        ListViewModel(ObserveEarthquakesUseCase(repository), repository, locationRepository)

    // uiState only produces values while someone is watching it, so the test watches it in the background
    private fun TestScope.startCollecting(viewModel: ListViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun refreshFailure_keepsSavedEarthquakes_andShowsTheError() = runTest {
        repository.nextRefreshResult = RefreshResult.Failure(DataError.NoConnection)
        val viewModel = createViewModel()
        startCollecting(viewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // The offline-first promise: a failed refresh never removes what is already saved
        assertEquals(listOf("a"), state.earthquakes.map { it.earthquake.id })
        assertEquals(DataError.NoConnection, state.refreshError)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun successfulRetry_clearsTheError() = runTest {
        repository.nextRefreshResult = RefreshResult.Failure(DataError.Server(503))
        val viewModel = createViewModel()
        startCollecting(viewModel)
        advanceUntilIdle()

        repository.nextRefreshResult = RefreshResult.Success
        viewModel.refresh()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.refreshError)
        assertEquals(2, repository.refreshCount)
    }

    @Test
    fun withoutPermission_asksForLocation_andDecliningHidesThePrompt() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)
        advanceUntilIdle()
        assertEquals(LocationState.PermissionNeeded, viewModel.uiState.value.locationState)

        viewModel.onLocationPermissionResult(granted = false)
        advanceUntilIdle()
        assertEquals(LocationState.Declined, viewModel.uiState.value.locationState)
    }

    @Test
    fun magnitudeFilter_hidesWeakerEarthquakes_andAllShowsThemAgain() = runTest {
        // The single saved earthquake is M1.0, below the 2.5 threshold
        val viewModel = createViewModel()
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.onMagnitudeFilterSelected(MagnitudeFilter.MODERATE)
        advanceUntilIdle()
        assertEquals(MagnitudeFilter.MODERATE, viewModel.uiState.value.magnitudeFilter)
        assertTrue(viewModel.uiState.value.earthquakes.isEmpty())

        viewModel.onMagnitudeFilterSelected(MagnitudeFilter.ALL)
        advanceUntilIdle()
        assertEquals(listOf("a"), viewModel.uiState.value.earthquakes.map { it.earthquake.id })
    }

    @Test
    fun grantingPermission_makesLocationAvailable() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)
        advanceUntilIdle()

        locationRepository.permissionGranted = true
        locationRepository.location = UserLocation(0.0, 0.0)
        viewModel.onLocationPermissionResult(granted = true)
        viewModel.onSortSelected(SortOrder.NEAREST)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LocationState.Available(UserLocation(0.0, 0.0)), state.locationState)
        // The single earthquake is at 0,0, the same point as the user
        assertEquals(0.0, state.earthquakes.single().distanceKm!!, 0.001)
    }
}
