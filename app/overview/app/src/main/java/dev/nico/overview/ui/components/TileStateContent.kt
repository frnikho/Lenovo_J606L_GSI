package dev.nico.overview.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import dev.nico.overview.core.Formats
import dev.nico.overview.core.TileState
import dev.nico.overview.ui.theme.LocalOverviewColors

private const val STALE_ALPHA = 0.55f
private enum class Kind { LOADING, READY, STALE, ERROR }

/**
 * Fondu entre les genres d'état seulement : une nouvelle donnée « Ready » ne refait pas de fondu (les chiffres
 * défilent à l'intérieur).
 */
@Composable
fun <T> TileStateContent(
    state: TileState<T>,
    nowMs: Long,
    loadingText: String,
    content: @Composable (T) -> Unit,
) {
    val colors = LocalOverviewColors.current
    val kind = when (state) {
        TileState.Loading -> Kind.LOADING
        is TileState.Ready -> Kind.READY
        is TileState.Stale -> Kind.STALE
        is TileState.Error -> Kind.ERROR
    }
    Crossfade(targetState = kind, animationSpec = tween(300), label = "tile-state") { shown ->
        when {
            shown == Kind.READY && state is TileState.Ready -> content(state.data)
            shown == Kind.STALE && state is TileState.Stale -> Column(Modifier.alpha(STALE_ALPHA)) {
                content(state.data)
                Text(Formats.age(nowMs - state.updatedAtMs), color = colors.textSecondary, fontSize = 16.sp)
            }
            shown == Kind.ERROR && state is TileState.Error ->
                Text(state.message, color = colors.textSecondary, fontSize = 18.sp)
            else -> Text(loadingText, color = colors.textSecondary, fontSize = 18.sp)
        }
    }
}
