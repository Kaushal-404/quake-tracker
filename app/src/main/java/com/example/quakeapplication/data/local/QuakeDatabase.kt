package com.example.quakeapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Lists the tables and the layout version; the version must go up whenever a table changes shape
// exportSchema is off because this database is only a cache that can always be downloaded again
@Database(entities = [EarthquakeEntity::class], version = 1, exportSchema = false)
abstract class QuakeDatabase : RoomDatabase() {
    // Room writes the real implementation at build time
    abstract fun earthquakeDao(): EarthquakeDao
}