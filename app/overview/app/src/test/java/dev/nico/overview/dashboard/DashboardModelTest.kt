package dev.nico.overview.dashboard

import com.google.common.truth.Truth.assertThat
import dev.nico.overview.core.LatLon
import dev.nico.overview.core.TileState
import dev.nico.overview.fuel.FuelProvider
import dev.nico.overview.fuel.FuelStation
import dev.nico.overview.settings.Cached
import dev.nico.overview.settings.TileCacheStore
import dev.nico.overview.weather.Weather
import dev.nico.overview.weather.WeatherProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class DashboardModelTest {
    private val home = LatLon(47.9029, 1.9093)
    private val nearby = LatLon(47.9035, 1.9093) // ~70 m
    private val weather = Weather(21.5, 3, false, 19.9, 22.5, 1L, 2L, 83)
    private val station = FuelStation(1, "rue", "Olivet", LatLon(47.84, 1.91), 0.909, 5L, 6.9)

    private class MemoryCache(var weather: Cached<Weather>? = null) : TileCacheStore {
        var fuel: Cached<List<FuelStation>>? = null
        override suspend fun loadWeather() = weather
        override suspend fun saveWeather(value: Cached<Weather>) { weather = value }
        override suspend fun loadFuel() = fuel
        override suspend fun saveFuel(value: Cached<List<FuelStation>>) { fuel = value }
    }

    @Test
    fun `first position loads both tiles and caches them`() = runTest {
        val cache = MemoryCache()
        val model = DashboardModel(WeatherProvider { weather }, FuelProvider { listOf(station) }, cache) { 1_000 }
        model.onPosition(home)
        assertThat(model.weather.value).isEqualTo(TileState.Ready(weather, 1_000))
        assertThat(model.fuel.value).isEqualTo(TileState.Ready(listOf(station), 1_000))
        assertThat(cache.weather).isEqualTo(Cached(weather, 1_000))
    }

    @Test
    fun `small move shortly after does not refetch`() = runTest {
        var calls = 0
        var now = 0L
        val model = DashboardModel(WeatherProvider { calls++; weather }, FuelProvider { listOf(station) }, MemoryCache()) { now }
        model.onPosition(home)
        now = 60_000
        model.onPosition(nearby)
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun `failure after success keeps stale data`() = runTest {
        var fail = false
        var now = 0L
        val provider = WeatherProvider { if (fail) throw IOException("hors ligne") else weather }
        val model = DashboardModel(provider, FuelProvider { listOf(station) }, MemoryCache()) { now }
        model.onPosition(home)
        fail = true
        now = DashboardModel.WEATHER_MAX_AGE_MS
        model.onPosition(home)
        assertThat(model.weather.value).isEqualTo(TileState.Stale(weather, 0))
    }

    @Test
    fun `cached data is shown as stale before first fetch`() = runTest {
        val cache = MemoryCache(weather = Cached(weather, 500))
        val model = DashboardModel(WeatherProvider { weather }, FuelProvider { emptyList() }, cache) { 1_000 }
        model.restoreCache()
        assertThat(model.weather.value).isEqualTo(TileState.Stale(weather, 500))
        assertThat(model.fuel.value).isEqualTo(TileState.Loading)
    }
}
