package com.example.quakeapplication.data.repository

import com.example.quakeapplication.data.EarthquakeApi
import com.example.quakeapplication.data.local.EarthquakeDao
import com.example.quakeapplication.data.mapper.toDomain
import com.example.quakeapplication.data.mapper.toEntities
import com.example.quakeapplication.domain.model.DataError
import com.example.quakeapplication.domain.model.Earthquake
import com.example.quakeapplication.domain.model.RefreshResult
import com.example.quakeapplication.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// "Offline first": screens ONLY read from the database; the network only updates the database
// So a failed download can never wipe what the user already sees
// @Singleton: one shared copy, so the lock below covers every caller in the app
@Singleton
class OfflineFirstEarthquakeRepository @Inject constructor(
    private val api: EarthquakeApi,
    private val dao: EarthquakeDao,
) : EarthquakeRepository {
    // Two refreshes never run at once; a second caller waits instead of downloading in parallel
    private val refreshLock = Mutex()
    override fun observeEarthquakes(): Flow<List<Earthquake>> =
        dao.observeAll()
            .map { rows -> rows.map { row -> row.toDomain() } }
            // Room re-sends after ANY change to the table; this drops identical lists so screens skip redrawing
            .distinctUntilChanged()

    override fun observeEarthquake(id: String): Flow<Earthquake?> =
    dao.observeById(id)
       .map { row -> row?.toDomain() }
    .distinctUntilChanged()

    override suspend fun refresh(): RefreshResult  = refreshLock.withLock {
        try {
            // Safe from the main thread: OkHttp runs the request and parsing on its own threads
            val feed = api.fetchEarthquakesPerDay()
            // Also safe from the main thread: Room runs suspend functions on its own threads
            dao.replaceAll(feed.toEntities())
            RefreshResult.Success
        } catch (error: IOException) {
            // No answer at all: offline, airplane mode, timeout
            RefreshResult.Failure(DataError.NoConnection)
        } catch (error: HttpException) {
            // An answer with a failure status
            RefreshResult.Failure(DataError.Server(error.code()))
        } catch (error: SerializationException) {
            // An answer we cannot parse
            RefreshResult.Failure(DataError.UnexpectedData)
        }
        // Only these three types are caught on purpose
        // Coroutine cancellation is a different type and passes straight through,
        // so the work stops when the screen that started it goes away
    }

}