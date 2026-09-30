package dev.nico.overview.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nico.overview.R

@Immutable
data class OverviewColors(
    val background: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val accent: Color,
)

val DarkColors = OverviewColors(
    background = Color(0xFF000000),
    surface = Color(0xFF141517),
    text = Color(0xFFF2F3F5),
    textSecondary = Color(0xFF8E939B),
    accent = Color(0xFF3E6AE1),
)

val LightColors = OverviewColors(
    background = Color(0xFFF1F2F4),
    surface = Color(0xFFFFFFFF),
    text = Color(0xFF171A20),
    textSecondary = Color(0xFF5C5E62),
    accent = Color(0xFF3E6AE1),
)

val Inter = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

object Dimens {
    val gap = 16.dp
    val screenPadding = 24.dp
    val tilePadding = 24.dp
    val tileRadius = 20.dp
}

val LocalOverviewColors = staticCompositionLocalOf { DarkColors }

private const val THEME_FADE_MS = 600

@Composable
fun OverviewTheme(dark: Boolean, content: @Composable () -> Unit) {
    val target = if (dark) DarkColors else LightColors
    val colors = OverviewColors(
        background = fade(target.background),
        surface = fade(target.surface),
        text = fade(target.text),
        textSecondary = fade(target.textSecondary),
        accent = target.accent,
    )
    CompositionLocalProvider(
        LocalOverviewColors provides colors,
        LocalContentColor provides colors.text,
        LocalTextStyle provides TextStyle(fontFamily = Inter, color = colors.text),
        content = content,
    )
}

@Composable
private fun fade(color: Color): Color = animateColorAsState(color, tween(THEME_FADE_MS), label = "theme").value
