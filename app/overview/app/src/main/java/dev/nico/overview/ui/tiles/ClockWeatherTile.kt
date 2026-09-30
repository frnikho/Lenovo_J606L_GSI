package dev.nico.overview.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nico.overview.core.Formats
import dev.nico.overview.core.TileState
import dev.nico.overview.ui.components.RollingText
import dev.nico.overview.ui.components.TileCard
import dev.nico.overview.ui.components.TileStateContent
import dev.nico.overview.ui.theme.Inter
import dev.nico.overview.ui.theme.LocalOverviewColors
import dev.nico.overview.weather.Weather
import java.time.ZoneId

private const val RAIN_ALERT_PERCENT = 50

@Composable
fun ClockWeatherTile(state: TileState<Weather>, nowMs: Long, zone: ZoneId, modifier: Modifier = Modifier) {
    val colors = LocalOverviewColors.current
    TileCard(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            RollingText(
                Formats.clock(nowMs, zone),
                TextStyle(fontFamily = Inter, fontSize = 72.sp, fontWeight = FontWeight.Light, color = colors.text),
            )
            TileStateContent(state, nowMs, loadingText = "Météo…") { weather ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(WeatherIcons.of(weather.kind, weather.isDay)),
                            contentDescription = weather.kind.label,
                            tint = colors.text,
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        RollingText(
                            Formats.temperature(weather.temperatureC),
                            TextStyle(fontFamily = Inter, fontSize = 44.sp, fontWeight = FontWeight.Medium, color = colors.text),
                        )
                    }
                    Text(
                        "${weather.kind.label} · ${Formats.temperature(weather.minC)} / ${Formats.temperature(weather.maxC)}",
                        color = colors.textSecondary,
                        fontSize = 18.sp,
                    )
                    if (weather.rainChancePercent >= RAIN_ALERT_PERCENT) {
                        Text("Pluie probable · ${weather.rainChancePercent} %", color = colors.accent, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
