package dev.nico.overview

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.nico.overview.dashboard.DashboardModel
import dev.nico.overview.drivemode.ActivityDashboardLauncher
import dev.nico.overview.drivemode.DriveModeController
import dev.nico.overview.drivemode.TrustBridge
import dev.nico.overview.fuel.FuelRepository
import dev.nico.overview.media.MediaSource
import dev.nico.overview.net.OkHttpClientAdapter
import dev.nico.overview.sensors.LightSensorSource
import dev.nico.overview.sensors.LocationSource
import dev.nico.overview.settings.SettingsStore
import dev.nico.overview.settings.TileCache
import dev.nico.overview.system.SuShell
import dev.nico.overview.weather.WeatherRepository
import dev.nico.overview.wifi.RootHotspotConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppGraph(context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val dataStore = PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("overview") }
    val settings = SettingsStore(dataStore)
    private val http = OkHttpClientAdapter()

    val dashboard = DashboardModel(WeatherRepository(http), FuelRepository(http), TileCache(dataStore))
    val location = LocationSource(context)
    val light = LightSensorSource(context)
    val media = MediaSource(context)
    val launcher = ActivityDashboardLauncher(context)
    val driveMode = DriveModeController(
        scope = appScope,
        trust = TrustBridge,
        hotspot = RootHotspotConnector(SuShell()) { settings.current().hotspot },
        launcher = launcher,
    )
}
