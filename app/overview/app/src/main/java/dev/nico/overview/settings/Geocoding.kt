package dev.nico.overview.settings

import android.content.Context
import android.location.Address
import android.location.Geocoder
import dev.nico.overview.core.LatLon
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/** Adresse → coordonnées (service Google via les GApps). null si introuvable. */
suspend fun geocode(context: Context, address: String): LatLon? = suspendCancellableCoroutine { cont ->
    Geocoder(context, Locale.FRANCE).getFromLocationName(
        address,
        1,
        object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                cont.resume(addresses.firstOrNull()?.let { LatLon(it.latitude, it.longitude) })
            }

            override fun onError(errorMessage: String?) {
                cont.resume(null)
            }
        },
    )
}
