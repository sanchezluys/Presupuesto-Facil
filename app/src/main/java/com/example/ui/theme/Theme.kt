package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = SlateSecondary,
    onSecondary = SlateOnSecondary,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = SlateOnSecondaryContainer,
    tertiary = SlateTertiary,
    onTertiary = SlateOnTertiary,
    background = ArtisticBackground,
    onBackground = ArtisticOnBackground,
    surface = ArtisticSurface,
    onSurface = ArtisticOnSurface,
    surfaceVariant = ArtisticSurfaceVariant,
    onSurfaceVariant = ArtisticOnSurfaceVariant,
    outline = ArtisticOutline,
    outlineVariant = ArtisticOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryContainer,
    onPrimary = BlueOnPrimaryContainer,
    primaryContainer = BluePrimary,
    onPrimaryContainer = BlueOnPrimary,
    secondary = SlateSecondaryContainer,
    onSecondary = SlateOnSecondaryContainer,
    secondaryContainer = SlateSecondary,
    onSecondaryContainer = SlateOnSecondary,
    background = ArtisticOnBackground,
    onBackground = ArtisticBackground,
    surface = SlateTertiary,
    onSurface = ArtisticBackground,
    surfaceVariant = SlateSecondary,
    onSurfaceVariant = SlateSecondaryContainer,
    outline = ArtisticOutlineVariant
)

val ArtisticShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ArtisticShapes,
        content = content
    )
}
