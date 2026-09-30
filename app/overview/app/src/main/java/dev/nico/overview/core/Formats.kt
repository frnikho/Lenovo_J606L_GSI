package dev.nico.overview.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object Formats {
    private val FR = Locale.FRANCE
    private const val SHORT_DISTANCE_KM = 10.0
    private const val MINUTE_MS = 60_000L
    private const val HOUR_MIN = 60L
    private val CLOCK = DateTimeFormatter.ofPattern("HH:mm")

    fun price(eur: Double): String = String.format(FR, "%.3f €", eur)

    fun distance(km: Double): String =
        if (km < SHORT_DISTANCE_KM) String.format(FR, "%.1f km", km) else "${km.roundToInt()} km"

    fun temperature(celsius: Double): String {
        val rounded = celsius.roundToInt()
        return "${if (rounded == 0) 0 else rounded}°"
    }

    fun age(elapsedMs: Long): String {
        val minutes = elapsedMs / MINUTE_MS
        return when {
            minutes < 1 -> "à l'instant"
            minutes < HOUR_MIN -> "il y a $minutes min"
            else -> "il y a ${minutes / HOUR_MIN} h"
        }
    }

    fun clock(epochMs: Long, zone: ZoneId): String = CLOCK.withZone(zone).format(Instant.ofEpochMilli(epochMs))
}
