package dev.nico.overview.settings

import dev.nico.overview.core.LatLon
import dev.nico.overview.theme.ThemeMode
import dev.nico.overview.wifi.HotspotSettings
import kotlinx.serialization.Serializable

enum class PlaceIcon { HOME, WORK, STAR }

@Serializable
data class Place(val name: String, val position: LatLon, val icon: PlaceIcon = PlaceIcon.STAR)

data class Settings(val places: List<Place>, val hotspot: HotspotSettings, val themeMode: ThemeMode)
