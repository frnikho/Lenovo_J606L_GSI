package dev.nico.overview.drivemode

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriveModeControllerTest {
    private class FakeTrust : TrustControl {
        val calls = mutableListOf<Boolean>()
        override fun setTrusted(trusted: Boolean) { calls += trusted }
    }

    private class FakeHotspot(private val result: Boolean) : HotspotConnector {
        var calls = 0
        override suspend fun ensureConnected(): Boolean { calls++; return result }
    }

    private class FakeLauncher : DashboardLauncher {
        val calls = mutableListOf<String>()
        override fun show() { calls += "show" }
        override fun close() { calls += "close" }
    }

    private fun TestScope.controller(trust: FakeTrust, hotspot: FakeHotspot, launcher: FakeLauncher) =
        DriveModeController(backgroundScope, trust, hotspot, launcher) { testScheduler.currentTime }

    @Test
    fun `connection grants trust, shows dashboard and joins hotspot`() = runTest {
        val trust = FakeTrust(); val hotspot = FakeHotspot(true); val launcher = FakeLauncher()
        val c = controller(trust, hotspot, launcher)
        c.onCarConnection(true)
        runCurrent()
        assertThat(trust.calls).containsExactly(true)
        assertThat(launcher.calls).containsExactly("show")
        assertThat(hotspot.calls).isEqualTo(1)
        assertThat(c.hotspotOk.value).isTrue()
        assertThat(c.state.value).isEqualTo(DriveState.Active)
    }

    @Test
    fun `disconnection exits after 10 seconds`() = runTest {
        val trust = FakeTrust(); val launcher = FakeLauncher()
        val c = controller(trust, FakeHotspot(true), launcher)
        c.onCarConnection(true)
        c.onCarConnection(false)
        advanceTimeBy(9_999); runCurrent()
        assertThat(launcher.calls).containsExactly("show")
        advanceTimeBy(1); runCurrent()
        assertThat(trust.calls).containsExactly(true, false).inOrder()
        assertThat(launcher.calls).containsExactly("show", "close").inOrder()
        assertThat(c.state.value).isEqualTo(DriveState.Idle)
    }

    @Test
    fun `short glitch does not exit`() = runTest {
        val trust = FakeTrust(); val launcher = FakeLauncher()
        val c = controller(trust, FakeHotspot(true), launcher)
        c.onCarConnection(true)
        c.onCarConnection(false)
        advanceTimeBy(3_000); runCurrent()
        c.onCarConnection(true)
        advanceTimeBy(20_000); runCurrent()
        assertThat(trust.calls).containsExactly(true)
        assertThat(launcher.calls).containsExactly("show")
        assertThat(c.state.value).isEqualTo(DriveState.Active)
    }

    @Test
    fun `hotspot failure is reported but drive mode continues`() = runTest {
        val c = controller(FakeTrust(), FakeHotspot(false), FakeLauncher())
        c.onCarConnection(true)
        runCurrent()
        assertThat(c.hotspotOk.value).isFalse()
        assertThat(c.state.value).isEqualTo(DriveState.Active)
    }
}
