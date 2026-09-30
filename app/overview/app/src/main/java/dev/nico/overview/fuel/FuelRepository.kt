package dev.nico.overview.fuel

import dev.nico.overview.core.LatLon
import dev.nico.overview.net.HttpClient

class FuelRepository(
    private val http: HttpClient,
    private val clock: () -> Long = System::currentTimeMillis,
) : FuelProvider {
    override suspend fun fetch(origin: LatLon): List<FuelStation> {
        val body = http.get(FuelApi.lpgUrl(origin, RADIUS_KM, FETCH_LIMIT))
        return FuelSelector.cheapest(FuelParser.parse(body, origin), clock(), MAX_PRICE_AGE_MS, SHOWN)
    }

    companion object {
        const val RADIUS_KM = 20
        const val FETCH_LIMIT = 10
        const val SHOWN = 3
        const val MAX_PRICE_AGE_MS = 7L * 24 * 3_600_000
    }
}
