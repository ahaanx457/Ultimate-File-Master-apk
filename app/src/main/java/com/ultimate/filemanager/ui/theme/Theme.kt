package com.ultimate.filemanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * The app uses its own deliberate purple/indigo + blue + pink identity
 * (see Color.kt) rather than Android's per-device dynamic color, so the
 * brand looks the same and intentional on every device, light or dark.
 */
@Composable
fun UfmTheme(
    content: @Composable () -> Unit
) {

    val darkTheme = isSystemInDarkTheme()

    val colors = if (darkTheme) {
        ufmDarkColorScheme()
    } else {
        ufmLightColorScheme()
    }

    MaterialTheme(
        colorScheme = colors,
        typography = UfmTypography,
        content = content
    )
}
