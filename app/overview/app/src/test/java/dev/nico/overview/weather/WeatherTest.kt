package dev.nico.overview.weather

import com.google.common.truth.Truth.assertThat
import dev.nico.overview.core.LatLon
import dev.nico.overview.net.HttpClient
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class WeatherTest {
    private val body = javaClass.getResource("/weather.json")!!.readText()

    @Test
    fun `url asks current, hourly rain and daily sun times`() {
        val url = WeatherApi.url(LatLon(47.9029, 1.9093))
        assertThat(url).isEqualTo(
            "https://api.open-meteo.com/v1/forecast?latitude=47.9029&longitude=1.9093" +
                "&current=temperature_2m,weather_code,is_day&hourly=precipitation_probability" +
                "&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset" +
                "&timezone=auto&forecast_days=1&forecast_hours=3",
        )
    }

    @Test
    fun `parser reads current values and converts local sun times to epoch`() {
        val weather = WeatherParser.parse(body)
        assertThat(weather.temperatureC).isEqualTo(21.5)
        assertThat(weather.code).isEqualTo(3)
        assertThat(weather.isDay).isFalse()
        assertThat(weather.minC).isEqualTo(19.9)
        assertThat(weather.maxC).isEqualTo(22.5)
        assertThat(weather.rainChancePercent).isEqualTo(83)
        assertThat(weather.sunriseMs).isEqualTo(Instant.parse("2026-09-30T05:49:00Z").toEpochMilli())
        assertThat(weather.sunsetMs).isEqualTo(Instant.parse("2026-09-30T17:33:00Z").toEpochMilli())
    }

    @Test
    fun `wmo codes map to kinds`() {
        assertThat(WeatherCodes.kind(0)).isEqualTo(WeatherKind.CLEAR)
        assertThat(WeatherCodes.kind(2)).isEqualTo(WeatherKind.PARTLY_CLOUDY)
        assertThat(WeatherCodes.kind(3)).isEqualTo(WeatherKind.CLOUDY)
        assertThat(WeatherCodes.kind(45)).isEqualTo(WeatherKind.FOG)
        assertThat(WeatherCodes.kind(53)).isEqualTo(WeatherKind.DRIZZLE)
        assertThat(WeatherCodes.kind(81)).isEqualTo(WeatherKind.RAIN)
        assertThat(WeatherCodes.kind(75)).isEqualTo(WeatherKind.SNOW)
        assertThat(WeatherCodes.kind(95)).isEqualTo(WeatherKind.STORM)
        assertThat(WeatherCodes.kind(12345)).isEqualTo(WeatherKind.CLOUDY)
    }

    @Test
    fun `repository fetches and parses`() = runTest {
        val repo = WeatherRepository(HttpClient { body })
        assertThat(repo.fetch(LatLon(47.9, 1.9)).temperatureC).isEqualTo(21.5)
    }
}
