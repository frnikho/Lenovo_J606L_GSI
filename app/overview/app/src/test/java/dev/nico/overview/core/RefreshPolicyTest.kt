package dev.nico.overview.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RefreshPolicyTest {
    private val policy = RefreshPolicy(maxAgeMs = 600_000, minDistanceKm = 2.0)
    private val home = LatLon(47.9029, 1.9093)
    private val threeKmNorth = LatLon(47.9299, 1.9093)

    @Test
    fun `refreshes when never fetched`() {
        assertThat(policy.shouldRefresh(null, null, 0, home)).isTrue()
    }

    @Test
    fun `does not refresh when recent and close`() {
        assertThat(policy.shouldRefresh(1_000, home, 60_000, home)).isFalse()
    }

    @Test
    fun `refreshes when data is too old`() {
        assertThat(policy.shouldRefresh(0, home, 600_000, home)).isTrue()
    }

    @Test
    fun `refreshes when moved far enough`() {
        assertThat(policy.shouldRefresh(0, home, 60_000, threeKmNorth)).isTrue()
    }
}
