package com.keofi.poonamashishmehta_votergen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = lightColorScheme(
    primary = SaffronOrange,
    secondary = DarkNavy,
    tertiary = SaffronOrange,
    background = White,
    surface = White,
    surfaceVariant = LightSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onSurfaceVariant = PrimaryText,
    outline = DividerGray,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronOrange,
    secondary = DarkNavy,
    tertiary = SaffronOrange,
    background = White,
    surface = White,
    surfaceVariant = LightSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onSurfaceVariant = PrimaryText,
    outline = DividerGray,
    error = ErrorRed
)

@Composable
fun PoonamAshishMehtaVoterGenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides PrimaryText,
            LocalTextStyle provides Typography.bodyLarge.copy(color = PrimaryText),
            content = content
        )
    }
}
