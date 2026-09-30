package dev.nico.overview.ui.tiles

import androidx.annotation.DrawableRes
import dev.nico.overview.R
import dev.nico.overview.weather.WeatherKind

object WeatherIcons {
    @DrawableRes
    fun of(kind: WeatherKind, isDay: Boolean): Int = when (kind) {
        WeatherKind.CLEAR -> if (isDay) R.drawable.ic_sunny else R.drawable.ic_clear_night
        WeatherKind.PARTLY_CLOUDY -> if (isDay) R.drawable.ic_partly_cloudy_day else R.drawable.ic_partly_cloudy_night
        WeatherKind.CLOUDY -> R.drawable.ic_cloud
        WeatherKind.FOG -> R.drawable.ic_foggy
        WeatherKind.DRIZZLE -> R.drawable.ic_rainy_light
        WeatherKind.RAIN -> R.drawable.ic_rainy
        WeatherKind.SNOW -> R.drawable.ic_weather_snowy
        WeatherKind.STORM -> R.drawable.ic_thunderstorm
    }
}
