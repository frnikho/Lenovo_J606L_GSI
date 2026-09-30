package dev.nico.overview.core

import java.util.Locale

object WazeLink {
    const val PACKAGE = "com.waze"

    fun navigateUrl(to: LatLon): String =
        String.format(Locale.ROOT, "https://waze.com/ul?ll=%.6f,%.6f&navigate=yes", to.lat, to.lon)
}
