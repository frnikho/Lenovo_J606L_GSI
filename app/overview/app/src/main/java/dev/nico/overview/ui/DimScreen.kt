package dev.nico.overview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nico.overview.core.Formats
import dev.nico.overview.media.NowPlaying
import dev.nico.overview.ui.components.RollingText
import dev.nico.overview.ui.theme.Inter
import dev.nico.overview.weather.Weather
import java.time.ZoneId

private val DIM_TEXT = Color(0xFFB8BCC4)
private val DIM_SECONDARY = Color(0xFF6B7078)

/** Vue épurée, toujours sur fond noir ; un toucher la ferme (sans déclencher la tuile dessous). */
@Composable
fun DimScreen(nowMs: Long, zone: ZoneId, weather: Weather?, nowPlaying: NowPlaying?, onWake: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onWake),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RollingText(
            Formats.clock(nowMs, zone),
            TextStyle(fontFamily = Inter, fontSize = 180.sp, fontWeight = FontWeight.Light, color = DIM_TEXT),
        )
        weather?.let {
            Text("${Formats.temperature(it.temperatureC)} · ${it.kind.label}", color = DIM_SECONDARY, fontSize = 32.sp, fontFamily = Inter)
        }
        nowPlaying?.let {
            Text(
                "${it.title} — ${it.artist}",
                color = DIM_SECONDARY,
                fontSize = 26.sp,
                fontFamily = Inter,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
