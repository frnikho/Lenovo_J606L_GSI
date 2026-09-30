package dev.nico.overview.drivemode

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DriveModeMachineTest {
    private fun reduce(state: DriveState, event: DriveEvent) = DriveModeMachine.reduce(state, event)

    @Test
    fun `connection enters drive mode`() {
        assertThat(reduce(DriveState.Idle, DriveEvent.CarConnected))
            .isEqualTo(Transition(DriveState.Active, DriveEffect.ENTER))
    }

    @Test
    fun `repeated connection has no effect`() {
        assertThat(reduce(DriveState.Active, DriveEvent.CarConnected))
            .isEqualTo(Transition(DriveState.Active, null))
    }

    @Test
    fun `disconnection starts leaving without effect`() {
        assertThat(reduce(DriveState.Active, DriveEvent.CarDisconnected(1_000)))
            .isEqualTo(Transition(DriveState.Leaving(1_000), null))
    }

    @Test
    fun `tick before 10 seconds keeps leaving`() {
        assertThat(reduce(DriveState.Leaving(1_000), DriveEvent.Tick(10_999)))
            .isEqualTo(Transition(DriveState.Leaving(1_000), null))
    }

    @Test
    fun `tick after 10 seconds exits`() {
        assertThat(reduce(DriveState.Leaving(1_000), DriveEvent.Tick(11_000)))
            .isEqualTo(Transition(DriveState.Idle, DriveEffect.EXIT))
    }

    @Test
    fun `reconnection while leaving cancels exit`() {
        assertThat(reduce(DriveState.Leaving(1_000), DriveEvent.CarConnected))
            .isEqualTo(Transition(DriveState.Active, null))
    }

    @Test
    fun `disconnection while idle is ignored`() {
        assertThat(reduce(DriveState.Idle, DriveEvent.CarDisconnected(5)))
            .isEqualTo(Transition(DriveState.Idle, null))
    }
}
