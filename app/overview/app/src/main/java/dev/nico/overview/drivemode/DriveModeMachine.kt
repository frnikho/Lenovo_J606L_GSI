package dev.nico.overview.drivemode

sealed interface DriveState {
    data object Idle : DriveState
    data object Active : DriveState
    data class Leaving(val sinceMs: Long) : DriveState
}

sealed interface DriveEvent {
    data object CarConnected : DriveEvent
    data class CarDisconnected(val atMs: Long) : DriveEvent
    data class Tick(val atMs: Long) : DriveEvent
}

enum class DriveEffect { ENTER, EXIT }

data class Transition(val state: DriveState, val effect: DriveEffect?)

object DriveModeMachine {
    /** Un faux contact plus court ne fait pas sortir du mode conduite. */
    const val EXIT_DELAY_MS = 10_000L

    fun reduce(state: DriveState, event: DriveEvent): Transition = when (event) {
        DriveEvent.CarConnected -> when (state) {
            DriveState.Idle -> Transition(DriveState.Active, DriveEffect.ENTER)
            else -> Transition(DriveState.Active, null)
        }
        is DriveEvent.CarDisconnected -> when (state) {
            DriveState.Active -> Transition(DriveState.Leaving(event.atMs), null)
            else -> Transition(state, null)
        }
        is DriveEvent.Tick ->
            if (state is DriveState.Leaving && event.atMs - state.sinceMs >= EXIT_DELAY_MS) {
                Transition(DriveState.Idle, DriveEffect.EXIT)
            } else {
                Transition(state, null)
            }
    }
}
