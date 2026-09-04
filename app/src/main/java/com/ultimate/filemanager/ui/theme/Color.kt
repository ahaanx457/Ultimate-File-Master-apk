package com.ultimate.filemanager.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------
// Core brand palette — purple/indigo primary, blue secondary, pink tertiary
// ---------------------------------------------------------------------

private val LightPrimary = Color(0xFF5B4FE0)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFE6E1FF)
private val LightOnPrimaryContainer = Color(0xFF1B1359)

private val LightSecondary = Color(0xFF2F7DF0)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFDCE8FF)
private val LightOnSecondaryContainer = Color(0xFF002E6A)

private val LightTertiary = Color(0xFFD6428A)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFFFD9EA)
private val LightOnTertiaryContainer = Color(0xFF5C0F3B)

private val LightBackground = Color(0xFFF6F4FD)
private val LightOnBackground = Color(0xFF1C1B22)
private val LightSurface = Color(0xFFFCFBFF)
private val LightOnSurface = Color(0xFF1C1B22)
private val LightSurfaceVariant = Color(0xFFE7E3F3)
private val LightOnSurfaceVariant = Color(0xFF48454F)
private val LightOutline = Color(0xFF79747E)

private val DarkPrimary = Color(0xFFC9BFFF)
private val DarkOnPrimary = Color(0xFF2E1F80)
private val DarkPrimaryContainer = Color(0xFF453999)
private val DarkOnPrimaryContainer = Color(0xFFE9E1FF)

private val DarkSecondary = Color(0xFFA9C8FF)
private val DarkOnSecondary = Color(0xFF00315C)
private val DarkSecondaryContainer = Color(0xFF14478A)
private val DarkOnSecondaryContainer = Color(0xFFD8E5FF)

private val DarkTertiary = Color(0xFFFFB0D8)
private val DarkOnTertiary = Color(0xFF5C0F3B)
private val DarkTertiaryContainer = Color(0xFF7C2955)
private val DarkOnTertiaryContainer = Color(0xFFFFD9EA)

private val DarkBackground = Color(0xFF15131C)
private val DarkOnBackground = Color(0xFFE7E1EC)
private val DarkSurface = Color(0xFF1D1B25)
private val DarkOnSurface = Color(0xFFE7E1EC)
private val DarkSurfaceVariant = Color(0xFF2B2836)
private val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
private val DarkOutline = Color(0xFF948F99)

fun ufmLightColorScheme(): ColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

fun ufmDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

/**
 * Per-category accent colors used across Home, the file browser, and
 * anywhere a file/folder type needs a consistent, recognizable color.
 * Kept separate from the Material color scheme since these are content
 * accents, not UI-chrome colors.
 */
object CategoryColors {
    val Documents = Color(0xFF2F6FED)
    val Images = Color(0xFFE0468B)
    val Videos = Color(0xFFF4511E)
    val Music = Color(0xFF2FA84F)
    val Archives = Color(0xFFC98A02)
    val Downloads = Color(0xFF00A0B0)
    val Code = Color(0xFF7C4DFF)
    val Other = Color(0xFF5C6B7A)
    val Folder = Color(0xFF5B4FE0)
}
