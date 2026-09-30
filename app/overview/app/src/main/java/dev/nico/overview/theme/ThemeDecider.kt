package dev.nico.overview.theme

enum class ThemeMode { AUTO, LIGHT, DARK }

object ThemeDecider {
    fun isDark(mode: ThemeMode, nowMs: Long, sunriseMs: Long?, sunsetMs: Long?, lowLight: Boolean): Boolean =
        when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AUTO -> lowLight || isNight(nowMs, sunriseMs, sunsetMs)
        }

    private fun isNight(nowMs: Long, sunriseMs: Long?, sunsetMs: Long?): Boolean =
        sunriseMs != null && sunsetMs != null && (nowMs < sunriseMs || nowMs >= sunsetMs)
}

/**
 * Tunnel/parking : sombre après [toDarkMs] sous [darkBelowLux], clair seulement après [toLightMs] au-dessus de
 * [lightAboveLux] (retour lent pour ne pas clignoter sous les ponts).
 */
class LightHysteresis(
    private val darkBelowLux: Float = 15f,
    private val lightAboveLux: Float = 60f,
    private val toDarkMs: Long = 5_000,
    private val toLightMs: Long = 20_000,
) {
    private var lowLight = false
    private var candidateSinceMs: Long? = null

    fun update(lux: Float, nowMs: Long): Boolean {
        val wantsChange = if (lowLight) lux > lightAboveLux else lux < darkBelowLux
        if (!wantsChange) {
            candidateSinceMs = null
            return lowLight
        }
        val since = candidateSinceMs ?: nowMs.also { candidateSinceMs = it }
        val needed = if (lowLight) toLightMs else toDarkMs
        if (nowMs - since >= needed) {
            lowLight = !lowLight
            candidateSinceMs = null
        }
        return lowLight
    }
}
