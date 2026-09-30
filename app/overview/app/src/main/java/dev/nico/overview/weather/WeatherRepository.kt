package dev.nico.overview.weather

import dev.nico.overview.core.LatLon
import dev.nico.overview.net.HttpClient

class WeatherRepository(private val http: HttpClient) : WeatherProvider {
    override suspend fun fetch(position: LatLon): Weather = WeatherParser.parse(http.get(WeatherApi.url(position)))
}
