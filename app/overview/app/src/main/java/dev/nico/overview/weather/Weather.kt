package dev.nico.overview.weather

import dev.nico.overview.core.LatLon
import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    val temperatureC: Double,
    val code: Int,
    val isDay: Boolean,
    val minC: Double,
    val maxC: Double,
    val sunriseMs: Long,
    val sunsetMs: Long,
    val rainChancePercent: Int,
) {
    val kind: WeatherKind get() = WeatherCodes.kind(code)
}

fun interface WeatherProvider {
    suspend fun fetch(position: LatLon): Weather
}
