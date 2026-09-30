package dev.nico.overview.wifi

enum class WifiSecurity(val cmdName: String) { WPA2("wpa2"), WPA3("wpa3") }

data class HotspotSettings(val ssid: String, val security: WifiSecurity, val passphrase: String) {
    companion object {
        const val DEFAULT_SSID = "Pixel_9798"
    }
}
