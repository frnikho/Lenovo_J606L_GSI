package dev.nico.overview.wifi

object WifiStatusParser {
    private val CONNECTED = Regex("""Wifi is connected to "([^"]*)"""")

    /** Sortie de `cmd wifi status`. */
    fun connectedSsid(statusOutput: String): String? = CONNECTED.find(statusOutput)?.groupValues?.get(1)
}
