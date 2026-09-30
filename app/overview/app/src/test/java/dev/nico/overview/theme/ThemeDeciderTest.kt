package dev.nico.overview.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThemeDeciderTest {
    private val sunrise = 7_000L
    private val sunset = 19_000L

    @Test
    fun `manual modes win`() {
        assertThat(ThemeDecider.isDark(ThemeMode.LIGHT, 23_000, sunrise, sunset, lowLight = true)).isFalse()
        assertThat(ThemeDecider.isDark(ThemeMode.DARK, 12_000, sunrise, sunset, lowLight = false)).isTrue()
    }

    @Test
    fun `auto follows the sun`() {
        assertThat(ThemeDecider.isDark(ThemeMode.AUTO, 12_000, sunrise, sunset, false)).isFalse()
        assertThat(ThemeDecider.isDark(ThemeMode.AUTO, 20_000, sunrise, sunset, false)).isTrue()
        assertThat(ThemeDecider.isDark(ThemeMode.AUTO, 3_000, sunrise, sunset, false)).isTrue()
    }

    @Test
    fun `auto goes dark in low light during the day`() {
        assertThat(ThemeDecider.isDark(ThemeMode.AUTO, 12_000, sunrise, sunset, lowLight = true)).isTrue()
    }

    @Test
    fun `auto without sun times stays light unless low light`() {
        assertThat(ThemeDecider.isDark(ThemeMode.AUTO, 12_000, null, null, false)).isFalse()
    }

    @Test
    fun `low light needs 5 seconds of darkness`() {
        val h = LightHysteresis()
        assertThat(h.update(5f, 0)).isFalse()
        assertThat(h.update(5f, 4_999)).isFalse()
        assertThat(h.update(5f, 5_000)).isTrue()
    }

    @Test
    fun `short bright flash in a tunnel does not switch back`() {
        val h = LightHysteresis()
        h.update(5f, 0)
        h.update(5f, 5_000)
        assertThat(h.update(500f, 6_000)).isTrue()
        assertThat(h.update(5f, 7_000)).isTrue()
        assertThat(h.update(500f, 8_000)).isTrue()
        assertThat(h.update(500f, 27_999)).isTrue()
        assertThat(h.update(500f, 28_000)).isFalse()
    }

    @Test
    fun `mid-range light keeps current state`() {
        val h = LightHysteresis()
        assertThat(h.update(30f, 0)).isFalse()
        assertThat(h.update(30f, 60_000)).isFalse()
    }
}
