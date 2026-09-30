package dev.nico.overview.dashboard

import dev.nico.overview.core.LatLon
import dev.nico.overview.core.RefreshPolicy
import dev.nico.overview.core.TileState
import dev.nico.overview.core.afterFailure
import dev.nico.overview.fuel.FuelProvider
import dev.nico.overview.fuel.FuelStation
import dev.nico.overview.settings.Cached
import dev.nico.overview.settings.TileCacheStore
import dev.nico.overview.weather.Weather
import dev.nico.overview.weather.WeatherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class DashboardModel(
    private val weatherProvider: WeatherProvider,
    private val fuelProvider: FuelProvider,
    private val cache: TileCacheStore,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _weather = MutableStateFlow<TileState<Weather>>(TileState.Loading)
    val weather: StateFlow<TileState<Weather>> = _weather.asStateFlow()

    private val _fuel = MutableStateFlow<TileState<List<FuelStation>>>(TileState.Loading)
    val fuel: StateFlow<TileState<List<FuelStation>>> = _fuel.asStateFlow()

    private val weatherPolicy = RefreshPolicy(WEATHER_MAX_AGE_MS, WEATHER_MIN_KM)
    private val fuelPolicy = RefreshPolicy(FUEL_MAX_AGE_MS, FUEL_MIN_KM)
    private var weatherAt: Long? = null
    private var weatherPosition: LatLon? = null
    private var fuelAt: Long? = null
    private var fuelPosition: LatLon? = null

    /** Tourne tant que l'écran est affiché : position + tic régulier (pour rafraîchir à l'arrêt). */
    suspend fun run(positions: Flow<LatLon>, ticks: Flow<Unit>) {
        restoreCache()
        combine(positions, ticks) { position, _ -> position }.collect { onPosition(it) }
    }

    suspend fun restoreCache() {
        if (_weather.value == TileState.Loading) {
            cache.loadWeather()?.let { _weather.value = TileState.Stale(it.data, it.atMs) }
        }
        if (_fuel.value == TileState.Loading) {
            cache.loadFuel()?.let { _fuel.value = TileState.Stale(it.data, it.atMs) }
        }
    }

    suspend fun onPosition(position: LatLon) {
        val now = clock()
        if (weatherPolicy.shouldRefresh(weatherAt, weatherPosition, now, position)) refreshWeather(position, now)
        if (fuelPolicy.shouldRefresh(fuelAt, fuelPosition, now, position)) refreshFuel(position, now)
    }

    private suspend fun refreshWeather(position: LatLon, now: Long) {
        try {
            val data = weatherProvider.fetch(position)
            weatherAt = now
            weatherPosition = position
            _weather.value = TileState.Ready(data, now)
            cache.saveWeather(Cached(data, now))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _weather.value = _weather.value.afterFailure(e.message ?: NETWORK_ERROR)
        }
    }

    private suspend fun refreshFuel(position: LatLon, now: Long) {
        try {
            val data = fuelProvider.fetch(position)
            fuelAt = now
            fuelPosition = position
            _fuel.value = TileState.Ready(data, now)
            cache.saveFuel(Cached(data, now))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _fuel.value = _fuel.value.afterFailure(e.message ?: NETWORK_ERROR)
        }
    }

    companion object {
        const val WEATHER_MAX_AGE_MS = 15 * 60_000L
        const val WEATHER_MIN_KM = 5.0
        const val FUEL_MAX_AGE_MS = 10 * 60_000L
        const val FUEL_MIN_KM = 2.0
        private const val NETWORK_ERROR = "Réseau indisponible"
    }
}
