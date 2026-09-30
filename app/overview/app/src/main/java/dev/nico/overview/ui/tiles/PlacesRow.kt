package dev.nico.overview.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nico.overview.R
import dev.nico.overview.settings.Place
import dev.nico.overview.settings.PlaceIcon
import dev.nico.overview.ui.components.TileCard
import dev.nico.overview.ui.theme.Dimens
import dev.nico.overview.ui.theme.LocalOverviewColors

private val PLACE_WIDTH = 280.dp

@Composable
fun PlacesRow(places: List<Place>, onPlace: (Place) -> Unit, onSettings: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalOverviewColors.current
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
        items(places, key = { it.name }) { place ->
            TileCard(Modifier.width(PLACE_WIDTH).fillMaxHeight(), onClick = { onPlace(place) }) {
                Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(place.icon.drawable()), null, tint = colors.accent, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(place.name, fontSize = 24.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        item(key = "settings") {
            TileCard(Modifier.width(120.dp).fillMaxHeight(), onClick = onSettings) {
                Icon(
                    painterResource(if (places.isEmpty()) R.drawable.ic_add else R.drawable.ic_settings),
                    "Réglages",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(36.dp).align(Alignment.Center),
                )
            }
        }
    }
}

private fun PlaceIcon.drawable(): Int = when (this) {
    PlaceIcon.HOME -> R.drawable.ic_home
    PlaceIcon.WORK -> R.drawable.ic_work
    PlaceIcon.STAR -> R.drawable.ic_star
}
