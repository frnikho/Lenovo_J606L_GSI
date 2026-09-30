package dev.nico.overview.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId

class FormatsTest {
    @Test
    fun `price has three decimals and comma`() {
        assertThat(Formats.price(0.909)).isEqualTo("0,909 €")
    }

    @Test
    fun `short distance has one decimal`() {
        assertThat(Formats.distance(3.24)).isEqualTo("3,2 km")
    }

    @Test
    fun `long distance is rounded`() {
        assertThat(Formats.distance(15.6)).isEqualTo("16 km")
    }

    @Test
    fun `temperature is rounded with degree sign`() {
        assertThat(Formats.temperature(21.5)).isEqualTo("22°")
        assertThat(Formats.temperature(-0.4)).isEqualTo("0°")
    }

    @Test
    fun `age in minutes and hours`() {
        assertThat(Formats.age(30_000)).isEqualTo("à l'instant")
        assertThat(Formats.age(12 * 60_000)).isEqualTo("il y a 12 min")
        assertThat(Formats.age(3 * 3_600_000)).isEqualTo("il y a 3 h")
    }

    @Test
    fun `clock uses 24h format in zone`() {
        // 2026-09-30T18:05:00Z = 20:05 à Paris (UTC+2)
        assertThat(Formats.clock(1_790_791_500_000, ZoneId.of("Europe/Paris"))).isEqualTo("20:05")
    }
}
