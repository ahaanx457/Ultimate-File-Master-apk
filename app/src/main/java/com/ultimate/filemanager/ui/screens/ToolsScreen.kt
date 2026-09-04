package com.ultimate.filemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier
) {

    val tools = listOf(

        "Vault" to Icons.Outlined.Lock,

        "Archive Manager" to
                Icons.Outlined.Inventory2,

        "Search" to
                Icons.Outlined.Search,

        "Duplicate Finder" to
                Icons.Outlined.ContentCopy,

        "Downloader" to
                Icons.Outlined.Download,

        "Code / Text Editor" to
                Icons.Outlined.Code,

        "File Information" to
                Icons.Outlined.Info
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Tools",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(
            "Powerful utilities for your files.",
            style =
                MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            items(tools) { tool ->

                OutlinedCard(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    ListItem(

                        headlineContent = {
                            Text(tool.first)
                        },

                        supportingContent = {
                            Text("Phase 1 foundation")
                        },

                        leadingContent = {
                            Icon(
                                tool.second,
                                contentDescription =
                                    tool.first
                            )
                        }
                    )
                }
            }
        }
    }
}
