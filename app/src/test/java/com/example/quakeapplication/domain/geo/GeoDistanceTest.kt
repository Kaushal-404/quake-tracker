package com.example.quakeapplication.domain.geo

import com.example.quakeapplication.domain.geo.kilometersBetween
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoDistanceTest {

    @Test
    fun samePoint_isZero() {
        assertEquals(0.0, kilometersBetween(37.77, -122.42, 37.77, -122.42), 0.001)
    }

    @Test
    fun sanFranciscoToLosAngeles_isAbout559Km() {
        val distance = kilometersBetween(37.7749, -122.4194, 34.0522, -118.2437)
        // Published great-circle distance is about 559 km; allow a few km for the sphere approximation
        assertEquals(559.0, distance, 5.0)
    }
}
