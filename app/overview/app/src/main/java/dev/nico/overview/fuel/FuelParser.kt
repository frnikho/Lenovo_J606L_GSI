package dev.nico.overview.fuel

import dev.nico.overview.core.LatLon
import dev.nico.overview.core.distanceKm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime

object FuelParser {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class Response(val results: List<Record> = emptyList())

    @Serializable
    private data class Record(
        val id: Long,
        val adresse: String? = null,
        val ville: String? = null,
        val geom: Geom? = null,
        @SerialName("gplc_prix") val price: Double? = null,
        @SerialName("gplc_maj") val updated: String? = null,
    )

    @Serializable
    private data class Geom(val lat: Double, val lon: Double)

    fun parse(body: String, origin: LatLon): List<FuelStation> =
        json.decodeFromString<Response>(body).results.mapNotNull { record ->
            val geom = record.geom ?: return@mapNotNull null
            val price = record.price ?: return@mapNotNull null
            val updated = record.updated ?: return@mapNotNull null
            val position = LatLon(geom.lat, geom.lon)
            FuelStation(
                id = record.id,
                address = record.adresse.orEmpty(),
                city = record.ville.orEmpty(),
                position = position,
                priceEur = price,
                updatedAtMs = OffsetDateTime.parse(updated).toInstant().toEpochMilli(),
                distanceKm = distanceKm(origin, position),
            )
        }
}

object FuelSelector {
    fun cheapest(stations: List<FuelStation>, nowMs: Long, maxAgeMs: Long, count: Int): List<FuelStation> =
        stations
            .filter { nowMs - it.updatedAtMs <= maxAgeMs }
            .sortedWith(compareBy<FuelStation> { it.priceEur }.thenBy { it.distanceKm })
            .take(count)
}
