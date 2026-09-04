package com.ultimate.filemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ultimate.filemanager.ui.components.AccentListCard
import com.ultimate.filemanager.ui.theme.CategoryColors

private data class Tool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: androidx.compose.ui.graphics.Color
)

@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier
) {

    val tools = listOf(
        Tool("Vault", "Secure, encrypted storage for private files", Icons.Outlined.Lock, CategoryColors.Code),
        Tool("Archive Manager", "Create and extract ZIP archives", Icons.Outlined.Inventory2, CategoryColors.Archives),
        Tool("Search", "Find files across your storage instantly", Icons.Outlined.Search, CategoryColors.Downloads),
        Tool("Duplicate Finder", "Free up space by finding repeated files", Icons.Outlined.ContentCopy, CategoryColors.Images),
        Tool("Downloader", "Download files from direct, public links", Icons.Outlined.Download, CategoryColors.Music),
        Tool("Code / Text Editor", "View and edit code and text files", Icons.Outlined.Code, CategoryColors.Documents),
        Tool("File Information", "Inspect detailed file and folder metadata", Icons.Outlined.Info, CategoryColors.Videos)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Tools",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Powerful utilities for your files.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tools) { tool ->
                AccentListCard(
                    title = tool.title,
                    description = tool.description,
                    icon = tool.icon,
                    accentColor = tool.accent
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
