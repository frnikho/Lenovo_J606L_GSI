package dev.nico.overview.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import dev.nico.overview.core.LatLon
import dev.nico.overview.fuel.FuelStation
import dev.nico.overview.theme.ThemeMode
import dev.nico.overview.weather.Weather
import dev.nico.overview.wifi.HotspotSettings
import dev.nico.overview.wifi.WifiSecurity
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsStoreTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun TestScope.dataStore() = PreferenceDataStoreFactory.create(scope = backgroundScope) {
        tmp.newFile("test.preferences_pb").also { it.delete() }
    }

    private val home = Place("Maison", LatLon(47.9, 1.9), PlaceIcon.HOME)

    @Test
    fun `places codec round trips and tolerates garbage`() {
        assertThat(PlacesCodec.decode(PlacesCodec.encode(listOf(home)))).containsExactly(home)
        assertThat(PlacesCodec.decode("pas du json")).isEmpty()
        assertThat(PlacesCodec.decode(null)).isEmpty()
    }

    @Test
    fun `defaults when nothing saved`() = runTest {
        val s = SettingsStore(dataStore()).current()
        assertThat(s.places).isEmpty()
        assertThat(s.hotspot).isEqualTo(HotspotSettings(HotspotSettings.DEFAULT_SSID, WifiSecurity.WPA3, ""))
        assertThat(s.themeMode).isEqualTo(ThemeMode.AUTO)
    }

    @Test
    fun `saves places, hotspot and theme`() = runTest {
        val store = SettingsStore(dataStore())
        store.setPlaces(listOf(home))
        store.setHotspot(HotspotSettings("Pixel_9798", WifiSecurity.WPA2, "secret"))
        store.setThemeMode(ThemeMode.DARK)
        val s = store.current()
        assertThat(s.places).containsExactly(home)
        assertThat(s.hotspot.security).isEqualTo(WifiSecurity.WPA2)
        assertThat(s.hotspot.passphrase).isEqualTo("secret")
        assertThat(s.themeMode).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `tile cache round trips weather and fuel`() = runTest {
        val cache = TileCache(dataStore())
        val weather = Weather(21.5, 3, false, 19.9, 22.5, 1L, 2L, 83)
        val station = FuelStation(1, "rue", "Olivet", LatLon(47.8, 1.9), 0.909, 5L, 6.9)
        cache.saveWeather(Cached(weather, 10))
        cache.saveFuel(Cached(listOf(station), 20))
        assertThat(cache.loadWeather()).isEqualTo(Cached(weather, 10))
        assertThat(cache.loadFuel()).isEqualTo(Cached(listOf(station), 20))
    }
}
