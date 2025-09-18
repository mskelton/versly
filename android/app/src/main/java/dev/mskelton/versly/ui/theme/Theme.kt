package dev.mskelton.versly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

fun themedColor(lightColor: Color, darkColor: Color): ColorScheme.() -> Color = {
    if (this == LightColorScheme) lightColor else darkColor
}

val ColorScheme.wordsOfJesus: Color
    get() = themedColor(versly_theme_light_wordsOfJesus, versly_theme_dark_wordsOfJesus)()

@Composable
fun VerslyTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
