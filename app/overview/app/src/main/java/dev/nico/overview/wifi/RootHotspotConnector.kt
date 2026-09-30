package dev.nico.overview.wifi

import dev.nico.overview.drivemode.HotspotConnector
import dev.nico.overview.system.Shell
import dev.nico.overview.system.shellQuote
import kotlinx.coroutines.delay

/**
 * Connexion forcée au point d'accès du Pixel. Ce choix « manuel » fait ensuite préférer le Pixel à la Freebox
 * tant qu'il est à portée : rien à restaurer à la sortie du mode conduite.
 */
class RootHotspotConnector(
    private val shell: Shell,
    private val settings: suspend () -> HotspotSettings,
) : HotspotConnector {
    override suspend fun ensureConnected(): Boolean {
        val config = settings()
        if (config.ssid.isBlank()) return false
        if (currentSsid() == config.ssid) return true
        if (config.passphrase.isBlank()) return false
        shell.run(
            "cmd wifi connect-network ${shellQuote(config.ssid)} ${config.security.cmdName} " +
                shellQuote(config.passphrase),
        )
        repeat(POLL_ATTEMPTS) {
            delay(POLL_INTERVAL_MS)
            if (currentSsid() == config.ssid) return true
        }
        return false
    }

    private suspend fun currentSsid(): String? = WifiStatusParser.connectedSsid(shell.run("cmd wifi status").output)

    private companion object {
        const val POLL_ATTEMPTS = 15
        const val POLL_INTERVAL_MS = 1_000L
    }
}
