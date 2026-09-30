package dev.nico.overview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.nico.overview.R
import dev.nico.overview.core.TileState
import dev.nico.overview.core.dataOrNull
import dev.nico.overview.fuel.FuelStation
import dev.nico.overview.media.NowPlaying
import dev.nico.overview.settings.Place
import dev.nico.overview.ui.theme.Dimens
import dev.nico.overview.ui.theme.LocalOverviewColors
import dev.nico.overview.ui.tiles.ClockWeatherTile
import dev.nico.overview.ui.tiles.FuelTile
import dev.nico.overview.ui.tiles.MediaTile
import dev.nico.overview.ui.tiles.PlacesRow
import dev.nico.overview.weather.Weather
import java.time.ZoneId

private val PLACES_HEIGHT = 128.dp
private const val DIM_FADE_MS = 800

data class DashboardUi(
    val nowMs: Long,
    val zone: ZoneId,
    val weather: TileState<Weather>,
    val fuel: TileState<List<FuelStation>>,
    val nowPlaying: NowPlaying?,
    val places: List<Place>,
    val hotspotOk: Boolean?,
    val dimmed: Boolean,
)

class DashboardActions(
    val onPlace: (Place) -> Unit,
    val onStation: (FuelStation) -> Unit,
    val onPlayPause: () -> Unit,
    val onNext: () -> Unit,
    val onPrevious: () -> Unit,
    val onSettings: () -> Unit,
    val onWake: () -> Unit,
)

@Composable
fun DashboardScreen(ui: DashboardUi, actions: DashboardActions) {
    val colors = LocalOverviewColors.current
    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(
            Modifier.fillMaxSize().padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.gap),
        ) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                MediaTile(
                    ui.nowPlaying, actions.onPlayPause, actions.onNext, actions.onPrevious,
                    Modifier.weight(1.25f).fillMaxHeight(),
                )
                ClockWeatherTile(ui.weather, ui.nowMs, ui.zone, Modifier.weight(1f).fillMaxHeight())
                FuelTile(ui.fuel, ui.nowMs, actions.onStation, Modifier.weight(1f).fillMaxHeight())
            }
            PlacesRow(ui.places, actions.onPlace, actions.onSettings, Modifier.height(PLACES_HEIGHT).fillMaxWidth())
        }
        if (ui.hotspotOk == false) {
            Icon(
                painterResource(R.drawable.ic_wifi_off),
                "Point d'accès du téléphone introuvable",
                tint = colors.textSecondary,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp),
            )
        }
        AnimatedVisibility(ui.dimmed, enter = fadeIn(tween(DIM_FADE_MS)), exit = fadeOut(tween(DIM_FADE_MS / 2))) {
            DimScreen(ui.nowMs, ui.zone, ui.weather.dataOrNull(), ui.nowPlaying, actions.onWake)
        }
    }
}
