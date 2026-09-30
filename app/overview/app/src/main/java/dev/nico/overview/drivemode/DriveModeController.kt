package dev.nico.overview.drivemode

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

fun interface TrustControl {
    fun setTrusted(trusted: Boolean)
}

fun interface HotspotConnector {
    /** true si la tablette est (ou vient d'être mise) sur le point d'accès du Pixel. */
    suspend fun ensureConnected(): Boolean
}

interface DashboardLauncher {
    fun show()
    fun close()
}

class DriveModeController(
    private val scope: CoroutineScope,
    private val trust: TrustControl,
    private val hotspot: HotspotConnector,
    private val launcher: DashboardLauncher,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _state = MutableStateFlow<DriveState>(DriveState.Idle)
    val state: StateFlow<DriveState> = _state.asStateFlow()

    private val _hotspotOk = MutableStateFlow<Boolean?>(null)
    /** null tant que la connexion au point d'accès n'a pas été tentée. */
    val hotspotOk: StateFlow<Boolean?> = _hotspotOk.asStateFlow()

    private var exitTimer: Job? = null

    fun onCarConnection(connected: Boolean) {
        if (connected) {
            exitTimer?.cancel()
            dispatch(DriveEvent.CarConnected)
        } else {
            dispatch(DriveEvent.CarDisconnected(clock()))
            exitTimer?.cancel()
            exitTimer = scope.launch {
                delay(DriveModeMachine.EXIT_DELAY_MS)
                dispatch(DriveEvent.Tick(clock()))
            }
        }
    }

    private fun dispatch(event: DriveEvent) {
        val transition = DriveModeMachine.reduce(_state.value, event)
        _state.value = transition.state
        when (transition.effect) {
            DriveEffect.ENTER -> enter()
            DriveEffect.EXIT -> exit()
            null -> Unit
        }
    }

    private fun enter() {
        trust.setTrusted(true)
        launcher.show()
        scope.launch { _hotspotOk.value = hotspot.ensureConnected() }
    }

    private fun exit() {
        trust.setTrusted(false)
        launcher.close()
        _hotspotOk.value = null
    }
}
