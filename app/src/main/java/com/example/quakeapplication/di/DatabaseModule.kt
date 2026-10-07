package com.example.quakeapplication.di

import android.content.Context
import androidx.room.Room
import com.example.quakeapplication.data.local.EarthquakeDao
import com.example.quakeapplication.data.local.QuakeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Exactly one database object: two would each think they own the file and miss each other's changes
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuakeDatabase =
        Room.databaseBuilder(context, QuakeDatabase::class.java, "quakes.db")
            // If a future version changes the table, wipe and re-download instead of crashing
            // Fine here because the table is only a cache of public data
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    // Handed out on its own, so the repository depends on the DAO and not the whole database
    @Provides
    fun provideEarthquakeDao(database: QuakeDatabase): EarthquakeDao = database.earthquakeDao()
}