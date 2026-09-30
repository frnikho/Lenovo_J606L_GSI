package dev.nico.overview.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneOffset

object WeatherParser {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class Response(
        @SerialName("utc_offset_seconds") val utcOffsetSeconds: Int,
        val current: Current,
        val hourly: Hourly,
        val daily: Daily,
    )

    @Serializable
    private data class Current(
        @SerialName("temperature_2m") val temperature: Double,
        @SerialName("weather_code") val code: Int,
        @SerialName("is_day") val isDay: Int,
    )

    @Serializable
    private data class Hourly(
        @SerialName("precipitation_probability") val rain: List<Int?> = emptyList(),
    )

    @Serializable
    private data class Daily(
        @SerialName("temperature_2m_max") val max: List<Double>,
        @SerialName("temperature_2m_min") val min: List<Double>,
        val sunrise: List<String>,
        val sunset: List<String>,
    )

    fun parse(body: String): Weather {
        val r = json.decodeFromString<Response>(body)
        val offset = ZoneOffset.ofTotalSeconds(r.utcOffsetSeconds)
        return Weather(
            temperatureC = r.current.temperature,
            code = r.current.code,
            isDay = r.current.isDay == 1,
            minC = r.daily.min.first(),
            maxC = r.daily.max.first(),
            sunriseMs = LocalDateTime.parse(r.daily.sunrise.first()).toInstant(offset).toEpochMilli(),
            sunsetMs = LocalDateTime.parse(r.daily.sunset.first()).toInstant(offset).toEpochMilli(),
            rainChancePercent = r.hourly.rain.filterNotNull().maxOrNull() ?: 0,
        )
    }
}
