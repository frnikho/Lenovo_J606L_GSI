package dev.nico.overview.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.nico.overview.fuel.FuelStation
import dev.nico.overview.weather.Weather
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first

@Serializable
data class Cached<T>(val data: T, val atMs: Long)

interface TileCacheStore {
    suspend fun loadWeather(): Cached<Weather>?
    suspend fun saveWeather(value: Cached<Weather>)
    suspend fun loadFuel(): Cached<List<FuelStation>>?
    suspend fun saveFuel(value: Cached<List<FuelStation>>)
}

/** Dernières données reçues, affichées (avec leur âge) au démarrage ou sans réseau. */
class TileCache(private val store: DataStore<Preferences>) : TileCacheStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val weatherSerializer = Cached.serializer(Weather.serializer())
    private val fuelSerializer = Cached.serializer(ListSerializer(FuelStation.serializer()))

    override suspend fun loadWeather() = load(WEATHER, weatherSerializer)
    override suspend fun saveWeather(value: Cached<Weather>) = save(WEATHER, weatherSerializer, value)
    override suspend fun loadFuel() = load(FUEL, fuelSerializer)
    override suspend fun saveFuel(value: Cached<List<FuelStation>>) = save(FUEL, fuelSerializer, value)

    private suspend fun <T> load(key: Preferences.Key<String>, serializer: KSerializer<T>): T? =
        store.data.first()[key]?.let { raw -> runCatching { json.decodeFromString(serializer, raw) }.getOrNull() }

    private suspend fun <T> save(key: Preferences.Key<String>, serializer: KSerializer<T>, value: T) {
        store.edit { it[key] = json.encodeToString(serializer, value) }
    }

    private companion object {
        val WEATHER = stringPreferencesKey("cache_weather")
        val FUEL = stringPreferencesKey("cache_fuel")
    }
}
