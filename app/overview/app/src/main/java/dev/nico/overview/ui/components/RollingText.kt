package dev.nico.overview.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

private const val ROLL_MS = 280

/** Chiffres qui défilent verticalement quand la valeur change (prix, température, heure). */
@Composable
fun RollingText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    Row(modifier) {
        text.forEachIndexed { index, char ->
            key(text.length - index) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        val up = targetState > initialState
                        (slideInVertically(tween(ROLL_MS)) { if (up) it else -it } + fadeIn(tween(ROLL_MS)))
                            .togetherWith(
                                slideOutVertically(tween(ROLL_MS)) { if (up) -it else it } + fadeOut(tween(ROLL_MS)),
                            )
                    },
                    label = "roll",
                ) { c -> Text(c.toString(), style = style) }
            }
        }
    }
}
