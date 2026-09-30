package dev.nico.overview.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import dev.nico.overview.core.LatLon
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Fournisseur « fused » de la plateforme (pas celui de Google). Permission accordée par le script d'installation. */
class LocationSource(private val context: Context) {
    @SuppressLint("MissingPermission")
    fun updates(): Flow<LatLon> = callbackFlow {
        val manager = context.getSystemService(LocationManager::class.java)
        val listener = LocationListener { trySend(LatLon(it.latitude, it.longitude)) }
        manager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)?.let { trySend(LatLon(it.latitude, it.longitude)) }
        val request = LocationRequest.Builder(INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
            .setQuality(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY)
            .build()
        manager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, request, context.mainExecutor, listener)
        awaitClose { manager.removeUpdates(listener) }
    }

    private companion object {
        const val INTERVAL_MS = 60_000L
        const val MIN_DISTANCE_M = 200f
    }
}
