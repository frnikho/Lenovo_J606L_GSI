package dev.nico.overview.weather

enum class WeatherKind(val label: String) {
    CLEAR("Dégagé"),
    PARTLY_CLOUDY("Éclaircies"),
    CLOUDY("Couvert"),
    FOG("Brouillard"),
    DRIZZLE("Bruine"),
    RAIN("Pluie"),
    SNOW("Neige"),
    STORM("Orage"),
}

/** Codes météo WMO utilisés par Open-Meteo. */
object WeatherCodes {
    fun kind(code: Int): WeatherKind = when (code) {
        0 -> WeatherKind.CLEAR
        1, 2 -> WeatherKind.PARTLY_CLOUDY
        3 -> WeatherKind.CLOUDY
        45, 48 -> WeatherKind.FOG
        in 51..57 -> WeatherKind.DRIZZLE
        in 61..67, in 80..82 -> WeatherKind.RAIN
        in 71..77, 85, 86 -> WeatherKind.SNOW
        in 95..99 -> WeatherKind.STORM
        else -> WeatherKind.CLOUDY
    }
}
