package com.ultimate.filemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ViewerScreen(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Viewer",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(18.dp)
            ) {

                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = null
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    "Unified File Viewer",
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    "File type detection, built-in viewers and Android Open With fallback will use a unified opening pipeline.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
