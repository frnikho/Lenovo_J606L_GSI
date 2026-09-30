package dev.nico.overview.core

import kotlinx.serialization.Serializable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Serializable
data class LatLon(val lat: Double, val lon: Double)

private const val EARTH_RADIUS_KM = 6371.0

/** Distance à vol d'oiseau (haversine). */
fun distanceKm(a: LatLon, b: LatLon): Double {
    val dLat = Math.toRadians(b.lat - a.lat)
    val dLon = Math.toRadians(b.lon - a.lon)
    val h = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLon / 2).pow(2)
    return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
}
