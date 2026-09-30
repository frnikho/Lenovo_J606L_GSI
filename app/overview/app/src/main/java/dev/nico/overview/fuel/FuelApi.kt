package dev.nico.overview.fuel

import dev.nico.overview.core.LatLon
import java.net.URLEncoder
import java.util.Locale

object FuelApi {
    private const val BASE = "https://data.economie.gouv.fr/api/explore/v2.1/catalog/datasets/" +
        "prix-des-carburants-en-france-flux-instantane-v2/records"
    private const val FIELDS = "id,adresse,ville,geom,gplc_prix,gplc_maj"

    fun lpgUrl(origin: LatLon, radiusKm: Int, limit: Int): String {
        val where = String.format(
            Locale.ROOT,
            "within_distance(geom, geom'POINT(%.5f %.5f)', %dkm) and gplc_prix is not null",
            origin.lon, origin.lat, radiusKm,
        )
        return "$BASE?where=${encode(where)}&order_by=gplc_prix&limit=$limit&select=${encode(FIELDS)}"
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")
}
