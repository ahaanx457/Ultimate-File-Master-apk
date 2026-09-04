package com.ultimate.filemanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun UfmTheme(
    content: @Composable () -> Unit
) {

    val darkTheme = isSystemInDarkTheme()

    val context = LocalContext.current

    val colors = when {

        Build.VERSION.SDK_INT >= 31 &&
                darkTheme ->
            dynamicDarkColorScheme(context)

        Build.VERSION.SDK_INT >= 31 &&
                !darkTheme ->
            dynamicLightColorScheme(context)

        darkTheme ->
            darkColorScheme()

        else ->
            lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
