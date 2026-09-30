package dev.nico.overview.ui.tiles

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nico.overview.R
import dev.nico.overview.media.NowPlaying
import dev.nico.overview.ui.components.TileCard
import dev.nico.overview.ui.theme.LocalOverviewColors

@Composable
fun MediaTile(
    nowPlaying: NowPlaying?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOverviewColors.current
    TileCard(modifier) {
        if (nowPlaying == null) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(R.drawable.ic_music_note), null, tint = colors.textSecondary, modifier = Modifier.size(56.dp))
                Text("Aucune lecture en cours", color = colors.textSecondary, fontSize = 20.sp)
            }
            return@TileCard
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Crossfade(targetState = nowPlaying.art, animationSpec = tween(500), label = "art", modifier = Modifier.weight(1f)) { art ->
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(colors.background),
                    contentAlignment = Alignment.Center,
                ) {
                    if (art != null) {
                        Image(art.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(painterResource(R.drawable.ic_music_note), null, tint = colors.textSecondary, modifier = Modifier.size(72.dp))
                    }
                }
            }
            Column(Modifier.fillMaxWidth()) {
                Text(nowPlaying.title, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(nowPlaying.artist, color = colors.textSecondary, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                ControlButton(R.drawable.ic_skip_previous, "Précédent", 44, onPrevious)
                ControlButton(if (nowPlaying.isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow, "Lecture", 64, onPlayPause)
                ControlButton(R.drawable.ic_skip_next, "Suivant", 44, onNext)
            }
        }
    }
}

@Composable
private fun ControlButton(@DrawableRes icon: Int, label: String, sizeDp: Int, onClick: () -> Unit) {
    val colors = LocalOverviewColors.current
    Box(
        Modifier.size((sizeDp + 32).dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), label, tint = colors.text, modifier = Modifier.size(sizeDp.dp).aspectRatio(1f))
    }
}
