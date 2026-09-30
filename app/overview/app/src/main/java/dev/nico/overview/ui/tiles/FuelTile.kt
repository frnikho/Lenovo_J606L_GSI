package dev.nico.overview.ui.tiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nico.overview.R
import dev.nico.overview.core.Formats
import dev.nico.overview.core.TileState
import dev.nico.overview.fuel.FuelStation
import dev.nico.overview.ui.components.RollingText
import dev.nico.overview.ui.components.TileCard
import dev.nico.overview.ui.components.TileStateContent
import dev.nico.overview.ui.theme.Inter
import dev.nico.overview.ui.theme.LocalOverviewColors

@Composable
fun FuelTile(
    state: TileState<List<FuelStation>>,
    nowMs: Long,
    onStation: (FuelStation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOverviewColors.current
    TileCard(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_local_gas_station), null, tint = colors.accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("GPL · 20 km", color = colors.textSecondary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            TileStateContent(state, nowMs, loadingText = "Recherche des stations…") { stations ->
                if (stations.isEmpty()) {
                    Text("Aucune station GPL à 20 km", color = colors.textSecondary, fontSize = 18.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        stations.forEach { station -> StationRow(station) { onStation(station) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationRow(station: FuelStation, onClick: () -> Unit) {
    val colors = LocalOverviewColors.current
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RollingText(
            Formats.price(station.priceEur),
            TextStyle(fontFamily = Inter, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = colors.text),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(Formats.distance(station.distanceKm), color = colors.text, fontSize = 18.sp)
            Text(station.city, color = colors.textSecondary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
