package dev.nico.overview.core

/** Rafraîchir si jamais chargé, si la donnée est trop vieille ou si l'on s'est assez déplacé. */
class RefreshPolicy(private val maxAgeMs: Long, private val minDistanceKm: Double) {
    fun shouldRefresh(lastAtMs: Long?, lastPosition: LatLon?, nowMs: Long, position: LatLon): Boolean =
        lastAtMs == null ||
            lastPosition == null ||
            nowMs - lastAtMs >= maxAgeMs ||
            distanceKm(lastPosition, position) >= minDistanceKm
}
