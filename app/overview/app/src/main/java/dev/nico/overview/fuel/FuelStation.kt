package dev.nico.overview.fuel

import dev.nico.overview.core.LatLon
import kotlinx.serialization.Serializable

@Serializable
data class FuelStation(
    val id: Long,
    val address: String,
    val city: String,
    val position: LatLon,
    val priceEur: Double,
    val updatedAtMs: Long,
    val distanceKm: Double,
)

fun interface FuelProvider {
    suspend fun fetch(origin: LatLon): List<FuelStation>
}
