package dev.nico.overview.drivemode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Lien entre le contrôleur et l'agent de confiance (même processus). */
object TrustBridge : TrustControl {
    private val _trusted = MutableStateFlow(false)
    val trusted: StateFlow<Boolean> = _trusted.asStateFlow()

    override fun setTrusted(trusted: Boolean) {
        _trusted.value = trusted
    }
}
