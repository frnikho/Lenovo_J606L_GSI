package dev.nico.overview.weather

import dev.nico.overview.core.LatLon
import java.util.Locale

object WeatherApi {
    fun url(position: LatLon): String = String.format(
        Locale.ROOT,
        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
            "&current=temperature_2m,weather_code,is_day&hourly=precipitation_probability" +
            "&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset" +
            "&timezone=auto&forecast_days=1&forecast_hours=3",
        position.lat, position.lon,
    )
}
