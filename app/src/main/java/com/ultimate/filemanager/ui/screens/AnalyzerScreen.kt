package com.ultimate.filemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnalyzerScreen(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Storage Analyzer",
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
                    Icons.Outlined.Analytics,
                    contentDescription = null
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    "Storage Analysis",
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    "The analyzer will scan accessible storage and calculate file usage, categories, large files and duplicates.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
