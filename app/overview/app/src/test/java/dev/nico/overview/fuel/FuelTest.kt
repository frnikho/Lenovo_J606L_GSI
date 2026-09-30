package dev.nico.overview.fuel

import com.google.common.truth.Truth.assertThat
import dev.nico.overview.core.LatLon
import dev.nico.overview.net.HttpClient
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class FuelTest {
    private val origin = LatLon(47.9029, 1.9093)
    private val now = Instant.parse("2026-09-30T18:00:00Z").toEpochMilli()
    private val body = javaClass.getResource("/fuel_lpg.json")!!.readText()

    @Test
    fun `url filters lpg within radius around origin`() {
        val url = FuelApi.lpgUrl(origin, radiusKm = 20, limit = 10)
        assertThat(url).startsWith(
            "https://data.economie.gouv.fr/api/explore/v2.1/catalog/datasets/" +
                "prix-des-carburants-en-france-flux-instantane-v2/records?where=",
        )
        assertThat(url).contains(
            "within_distance%28geom%2C%20geom%27POINT%281.90930%2047.90290%29%27%2C%2020km%29" +
                "%20and%20gplc_prix%20is%20not%20null",
        )
        assertThat(url).contains("&order_by=gplc_prix&limit=10")
    }

    @Test
    fun `parser skips records without position or price and computes distance`() {
        val stations = FuelParser.parse(body, origin)
        assertThat(stations.map { it.id }).containsExactly(45160006L, 45100008L, 45000001L, 45000003L)
        val olivet = stations.first { it.id == 45160006L }
        assertThat(olivet.city).isEqualTo("Olivet")
        assertThat(olivet.priceEur).isEqualTo(0.909)
        assertThat(olivet.distanceKm).isWithin(0.3).of(6.9)
        assertThat(olivet.updatedAtMs).isEqualTo(Instant.parse("2026-09-30T00:01:00Z").toEpochMilli())
    }

    @Test
    fun `selector drops prices older than a week and keeps three cheapest`() {
        val stations = FuelParser.parse(body, origin)
        val selected = FuelSelector.cheapest(stations, now, maxAgeMs = 7L * 24 * 3_600_000, count = 3)
        assertThat(selected.map { it.id }).containsExactly(45160006L, 45100008L, 45000003L).inOrder()
    }

    @Test
    fun `parser skips records with unparsable date`() {
        val json = """
            {
              "results": [
                {"id": 1, "adresse": "A", "ville": "V", "geom": {"lon": 1.9093, "lat": 47.9029}, "gplc_prix": 0.9, "gplc_maj": "2026-09-30T00:01:00+00:00"},
                {"id": 2, "adresse": "B", "ville": "W", "geom": {"lon": 1.9093, "lat": 47.9029}, "gplc_prix": 0.95, "gplc_maj": "pas-une-date"}
              ]
            }
        """.trimIndent()
        val stations = FuelParser.parse(json, origin)
        assertThat(stations.map { it.id }).containsExactly(1L)
    }

    @Test
    fun `repository fetches and selects`() = runTest {
        var requested = ""
        val http = HttpClient { url -> requested = url; body }
        val repo = FuelRepository(http) { now }
        val result = repo.fetch(origin)
        assertThat(requested).contains("limit=10")
        assertThat(result.map { it.id }).containsExactly(45160006L, 45100008L, 45000003L).inOrder()
    }
}
