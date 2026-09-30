package dev.nico.overview.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TileStateTest {
    @Test
    fun `failure after ready keeps data as stale`() {
        val state: TileState<Int> = TileState.Ready(42, 1_000)
        assertThat(state.afterFailure("réseau")).isEqualTo(TileState.Stale(42, 1_000))
    }

    @Test
    fun `failure without data is an error`() {
        val state: TileState<Int> = TileState.Loading
        assertThat(state.afterFailure("réseau")).isEqualTo(TileState.Error("réseau"))
    }

    @Test
    fun `failure after stale stays stale`() {
        val state: TileState<Int> = TileState.Stale(7, 5)
        assertThat(state.afterFailure("réseau")).isEqualTo(TileState.Stale(7, 5))
    }

    @Test
    fun `dataOrNull exposes data of ready and stale`() {
        assertThat(TileState.Ready(1, 0).dataOrNull()).isEqualTo(1)
        assertThat(TileState.Stale(2, 0).dataOrNull()).isEqualTo(2)
        assertThat(TileState.Error("x").dataOrNull() as Any?).isNull()
    }
}
