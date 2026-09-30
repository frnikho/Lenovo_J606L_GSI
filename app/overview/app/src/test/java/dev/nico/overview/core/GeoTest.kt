package dev.nico.overview.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GeoTest {
    private val paris = LatLon(48.8566, 2.3522)
    private val orleans = LatLon(47.9029, 1.9093)

    @Test
    fun `distance between Paris and Orleans is about 111 km`() {
        assertThat(distanceKm(paris, orleans)).isWithin(1.5).of(111.0)
    }

    @Test
    fun `distance to same point is zero`() {
        assertThat(distanceKm(paris, paris)).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `waze url navigates to coordinates with dot decimals`() {
        assertThat(WazeLink.navigateUrl(LatLon(47.841, 1.914)))
            .isEqualTo("https://waze.com/ul?ll=47.841000,1.914000&navigate=yes")
    }
}
