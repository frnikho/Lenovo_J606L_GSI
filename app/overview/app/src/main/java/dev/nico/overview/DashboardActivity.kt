package dev.nico.overview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.nico.overview.core.LatLon
import dev.nico.overview.core.WazeLink
import dev.nico.overview.core.dataOrNull
import dev.nico.overview.drivemode.DriveState
import dev.nico.overview.theme.ThemeDecider
import dev.nico.overview.theme.ThemeMode
import dev.nico.overview.ui.DashboardActions
import dev.nico.overview.ui.DashboardScreen
import dev.nico.overview.ui.DashboardUi
import dev.nico.overview.ui.SettingsScreen
import dev.nico.overview.ui.theme.OverviewTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.ZoneId

class DashboardActivity : ComponentActivity() {
    private val graph get() = (application as OverviewApp).graph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        observeDriveMode()
        setContent { Root() }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun observeDriveMode() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { graph.launcher.closeRequests.collect { finish() } }
                launch {
                    graph.driveMode.state.collect { state ->
                        if (state == DriveState.Idle) {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }
                }
                launch { graph.dashboard.run(graph.location.updates(), ticks(TICK_MS)) }
            }
        }
    }

    private fun ticks(periodMs: Long) = flow {
        while (true) {
            emit(Unit)
            delay(periodMs)
        }
    }

    /** La navigation part sur l'écran de la voiture ; l'overview revient devant sur la tablette. */
    private fun navigateTo(target: LatLon) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WazeLink.navigateUrl(target))).setPackage(WazeLink.PACKAGE))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Waze n'est pas installé", Toast.LENGTH_LONG).show()
            return
        }
        window.decorView.postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        }, RETURN_DELAY_MS)
    }

    private fun setDimBrightness(dimmed: Boolean) {
        window.attributes = window.attributes.apply {
            screenBrightness = if (dimmed) DIM_BRIGHTNESS else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
    }

    @Composable
    private fun Root() {
        val settings by graph.settings.settings.collectAsStateWithLifecycle(initialValue = null)
        val weather by graph.dashboard.weather.collectAsStateWithLifecycle()
        val fuel by graph.dashboard.fuel.collectAsStateWithLifecycle()
        val nowPlaying by remember { graph.media.nowPlaying() }.collectAsStateWithLifecycle(initialValue = null)
        val lowLight by remember { graph.light.lowLight() }.collectAsStateWithLifecycle(initialValue = false)
        val driveState by graph.driveMode.state.collectAsStateWithLifecycle()
        val hotspotOk by graph.driveMode.hotspotOk.collectAsStateWithLifecycle()
        val now by rememberNow(CLOCK_TICK_MS)

        var showSettings by remember { mutableStateOf(false) }
        var lastTouchMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var dimmed by remember { mutableStateOf(false) }
        val driving = driveState != DriveState.Idle

        LaunchedEffect(lastTouchMs, driving, showSettings) {
            dimmed = false
            if (driving && !showSettings) {
                delay(IDLE_DIM_MS)
                dimmed = true
            }
        }
        LaunchedEffect(dimmed) { setDimBrightness(dimmed) }

        val w = weather.dataOrNull()
        val dark = ThemeDecider.isDark(settings?.themeMode ?: ThemeMode.AUTO, now, w?.sunriseMs, w?.sunsetMs, lowLight)
        val current = settings ?: return

        Box(
            Modifier.fillMaxSize().pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        lastTouchMs = System.currentTimeMillis()
                    }
                }
            },
        ) {
            if (showSettings) {
                SettingsScreen(current, graph.settings, onClose = { showSettings = false })
            } else {
                OverviewTheme(dark) {
                    DashboardScreen(
                        DashboardUi(now, ZoneId.systemDefault(), weather, fuel, nowPlaying, current.places, hotspotOk, dimmed),
                        DashboardActions(
                            onPlace = { navigateTo(it.position) },
                            onStation = { navigateTo(it.position) },
                            onPlayPause = graph.media::playPause,
                            onNext = graph.media::next,
                            onPrevious = graph.media::previous,
                            onSettings = { showSettings = true },
                            onWake = { lastTouchMs = System.currentTimeMillis() },
                        ),
                    )
                }
            }
        }
    }

    @Composable
    private fun rememberNow(periodMs: Long): State<Long> = produceState(System.currentTimeMillis()) {
        while (true) {
            delay(periodMs - System.currentTimeMillis() % periodMs)
            value = System.currentTimeMillis()
        }
    }

    private companion object {
        const val TICK_MS = 60_000L
        const val CLOCK_TICK_MS = 1_000L
        const val IDLE_DIM_MS = 30_000L
        const val RETURN_DELAY_MS = 2_000L
        const val DIM_BRIGHTNESS = 0.02f
    }
}
