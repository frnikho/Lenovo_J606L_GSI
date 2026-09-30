package dev.nico.overview.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.nico.overview.theme.ThemeMode
import dev.nico.overview.wifi.HotspotSettings
import dev.nico.overview.wifi.WifiSecurity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsStore(private val store: DataStore<Preferences>) {
    val settings: Flow<Settings> = store.data.map(::toSettings)

    suspend fun current(): Settings = settings.first()

    suspend fun setPlaces(places: List<Place>) {
        store.edit { it[PLACES] = PlacesCodec.encode(places) }
    }

    suspend fun setHotspot(hotspot: HotspotSettings) {
        store.edit {
            it[SSID] = hotspot.ssid
            it[SECURITY] = hotspot.security.name
            it[PASSPHRASE] = hotspot.passphrase
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[THEME] = mode.name }
    }

    private fun toSettings(prefs: Preferences) = Settings(
        places = PlacesCodec.decode(prefs[PLACES]),
        hotspot = HotspotSettings(
            ssid = prefs[SSID] ?: HotspotSettings.DEFAULT_SSID,
            security = enumOrDefault(prefs[SECURITY], WifiSecurity.WPA3),
            passphrase = prefs[PASSPHRASE].orEmpty(),
        ),
        themeMode = enumOrDefault(prefs[THEME], ThemeMode.AUTO),
    )

    private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
        name?.let { runCatching { enumValueOf<E>(it) }.getOrNull() } ?: default

    private companion object {
        val PLACES = stringPreferencesKey("places")
        val SSID = stringPreferencesKey("hotspot_ssid")
        val SECURITY = stringPreferencesKey("hotspot_security")
        val PASSPHRASE = stringPreferencesKey("hotspot_passphrase")
        val THEME = stringPreferencesKey("theme_mode")
    }
}
