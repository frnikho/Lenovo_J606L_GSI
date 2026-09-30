package dev.nico.overview.wifi

import com.google.common.truth.Truth.assertThat
import dev.nico.overview.system.Shell
import dev.nico.overview.system.ShellResult
import dev.nico.overview.system.shellQuote
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RootHotspotConnectorTest {
    private val settings = HotspotSettings("Pixel_9798", WifiSecurity.WPA3, "mot de'passe")

    private class FakeShell(private val statusSequence: List<String>) : Shell {
        val commands = mutableListOf<String>()
        private var statusCalls = 0
        override suspend fun run(command: String): ShellResult {
            commands += command
            if (command == "cmd wifi status") {
                val out = statusSequence[minOf(statusCalls, statusSequence.lastIndex)]
                statusCalls++
                return ShellResult(0, out)
            }
            return ShellResult(0, "")
        }
    }

    private fun connected(ssid: String) = "Wifi is enabled\nWifi is connected to \"$ssid\"\nWifiInfo: SSID: \"$ssid\""

    @Test
    fun `parser extracts connected ssid`() {
        assertThat(WifiStatusParser.connectedSsid(connected("Freebox-665703"))).isEqualTo("Freebox-665703")
        assertThat(WifiStatusParser.connectedSsid("Wifi is enabled\nWifi is not connected")).isNull()
    }

    @Test
    fun `shell quote escapes single quotes`() {
        assertThat(shellQuote("mot de'passe")).isEqualTo("'mot de'\\''passe'")
    }

    @Test
    fun `already on hotspot does nothing`() = runTest {
        val shell = FakeShell(listOf(connected("Pixel_9798")))
        assertThat(RootHotspotConnector(shell) { settings }.ensureConnected()).isTrue()
        assertThat(shell.commands).containsExactly("cmd wifi status")
    }

    @Test
    fun `connects then waits until hotspot is joined`() = runTest {
        val shell = FakeShell(listOf(connected("Freebox-665703"), connected("Freebox-665703"), connected("Pixel_9798")))
        assertThat(RootHotspotConnector(shell) { settings }.ensureConnected()).isTrue()
        assertThat(shell.commands).contains("cmd wifi connect-network 'Pixel_9798' wpa3 'mot de'\\''passe'")
    }

    @Test
    fun `gives up after polling`() = runTest {
        val shell = FakeShell(listOf(connected("Freebox-665703")))
        assertThat(RootHotspotConnector(shell) { settings }.ensureConnected()).isFalse()
    }

    @Test
    fun `missing passphrase does not try to connect`() = runTest {
        val shell = FakeShell(listOf(connected("Freebox-665703")))
        val connector = RootHotspotConnector(shell) { settings.copy(passphrase = "") }
        assertThat(connector.ensureConnected()).isFalse()
        assertThat(shell.commands).containsExactly("cmd wifi status")
    }
}
