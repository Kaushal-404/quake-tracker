package com.example.quakeapplication.mapper

import com.example.quakeapplication.data.mapper.toEntities
import com.example.quakeapplication.data.mapper.toEntityOrNull
import com.example.quakeapplication.data.remote.EarthquakeDto
import com.example.quakeapplication.data.remote.FeatureDto
import com.example.quakeapplication.data.remote.GeometryDto
import com.example.quakeapplication.data.remote.PropertiesDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EarthquakeMappersTest {

    // A complete, valid earthquake; each test changes one thing
    private val validFeature = FeatureDto(
        id = "nc1",
        properties = PropertiesDto(mag = 4.2, place = "Ridgecrest, CA", time = 1_000L, url = null, tsunami = 1),
        geometry = GeometryDto(coordinates = listOf(-117.6, 35.7, 8.0)),
    )

    @Test
    fun validFeature_readsLongitudeAndLatitudeFromTheRightPositions() {
        val row = validFeature.toEntityOrNull()!!
        // Position 0 is longitude and 1 is latitude: the easiest thing to get backwards
        assertEquals(-117.6, row.longitude, 0.0)
        assertEquals(35.7, row.latitude, 0.0)
        assertEquals(8.0, row.depthKm, 0.0)
        assertTrue(row.hasTsunamiFlag)
    }

    @Test
    fun missingId_isDropped() {
        assertNull(validFeature.copy(id = null).toEntityOrNull())
    }

    @Test
    fun shortCoordinates_isDropped() {
        val broken = validFeature.copy(geometry = GeometryDto(coordinates = listOf(-117.6)))
        assertNull(broken.toEntityOrNull())
    }

    @Test
    fun nullMagnitude_isKeptAsNull() {
        val feature = validFeature.copy(properties = validFeature.properties!!.copy(mag = null))
        assertNull(feature.toEntityOrNull()!!.magnitude)
    }

    @Test
    fun feed_skipsBrokenFeaturesAndKeepsTheRest() {
        val feed = EarthquakeDto(features = listOf(validFeature, validFeature.copy(id = null)))
        assertEquals(listOf("nc1"), feed.toEntities().map { it.id })
    }
}
