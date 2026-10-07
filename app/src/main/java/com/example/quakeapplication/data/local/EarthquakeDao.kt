package com.example.quakeapplication.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// The only place in the app that contains SQL
// Room checks every query against the table at build time
@Dao
interface EarthquakeDao {
    // Sends the list now and again every time the table changes; this is what makes screens update by themselves
    // Not "suspend": a Flow does its work only when someone collects it
    @Query("SELECT * FROM earthquakes ORDER BY timeEpochMillis DESC")
    fun observeAll(): Flow<List<EarthquakeEntity>>

    @Query("SELECT * FROM earthquakes WHERE id = :id")
    fun observeById(id: String): Flow<EarthquakeEntity?>

    // Upsert: insert if the id is new, update if it exists, so a duplicate id cannot crash the save
    @Upsert
    suspend fun upsertAll(earthquakes: List<EarthquakeEntity>)

    @Query("DELETE FROM earthquakes")
    suspend fun deleteAll()

    // The feed is a complete picture of the past 24 hours, so we replace the table with it
    // That also removes events USGS has withdrawn and events older than a day
    // @Transaction makes both steps one change:
    // if the app dies halfway, SQLite rolls back, and watchers are told once, so no empty list flashes
    @Transaction
    suspend fun replaceAll(earthquakes: List<EarthquakeEntity>) {
        deleteAll()
        upsertAll(earthquakes)
    }
}