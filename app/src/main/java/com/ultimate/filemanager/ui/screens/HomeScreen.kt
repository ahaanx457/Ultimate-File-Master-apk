package com.ultimate.filemanager.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ultimate.filemanager.core.storage.FileOperations
import com.ultimate.filemanager.core.storage.StorageAccessManager
import com.ultimate.filemanager.ui.components.FileCategoryCard
import com.ultimate.filemanager.ui.components.ModernActionButton
import com.ultimate.filemanager.ui.components.SectionHeader
import com.ultimate.filemanager.ui.components.StorageOverviewCard
import com.ultimate.filemanager.ui.theme.CategoryColors

private data class FileCategory(
    val name: String,
    val icon: ImageVector,
    val accent: androidx.compose.ui.graphics.Color
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onBrowse: (treeUri: String, title: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    var grantedUri by rememberSaveable {
        mutableStateOf<String?>(
            context.contentResolver.persistedUriPermissions
                .firstOrNull()
                ?.uri
                ?.toString()
        )
    }

    val storageStats = remember {
        StorageAccessManager(context).getInternalStorageStats()
    }

    val folderPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                grantedUri = uri.toString()
            }
        }

    val categories = listOf(
        FileCategory("Documents", Icons.Outlined.Description, CategoryColors.Documents),
        FileCategory("Images", Icons.Outlined.Image, CategoryColors.Images),
        FileCategory("Videos", Icons.Outlined.Movie, CategoryColors.Videos),
        FileCategory("Music", Icons.Outlined.MusicNote, CategoryColors.Music),
        FileCategory("Archives", Icons.Outlined.Inventory2, CategoryColors.Archives),
        FileCategory("Downloads", Icons.Outlined.Download, CategoryColors.Downloads),
        FileCategory("Code", Icons.Outlined.Code, CategoryColors.Code),
        FileCategory("Other", Icons.Outlined.InsertDriveFile, CategoryColors.Other)
    )

    // Grouped in pairs so the grid can be drawn with plain Rows instead of
    // a nested LazyVerticalGrid, which avoids a well-known Compose
    // measurement crash when a lazy grid is nested inside another
    // scrollable/lazy container.
    val categoryRows = categories.chunked(2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        item {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ultimate File Manager",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Manage your files from one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            val usedLabel = FileOperations.formatSize(storageStats.usedBytes)
            val freeLabel = FileOperations.formatSize(storageStats.freeBytes)

            StorageOverviewCard(
                title = "Internal Storage",
                usedLabel = usedLabel,
                freeLabel = freeLabel,
                usedFraction = storageStats.usedFraction
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentUri = grantedUri

            if (currentUri != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModernActionButton(
                        text = "Browse Files",
                        icon = Icons.Outlined.FolderOpen,
                        onClick = { onBrowse(currentUri, "My Files") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { folderPicker.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Change Folder", fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                ModernActionButton(
                    text = "Grant Folder Access",
                    icon = Icons.Outlined.FolderOpen,
                    onClick = { folderPicker.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Quick Access")

            Spacer(modifier = Modifier.height(10.dp))
        }

        items(categoryRows) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { category ->
                    val currentUri = grantedUri
                    val enabled = currentUri != null
                    FileCategoryCard(
                        name = category.name,
                        icon = category.icon,
                        accentColor = category.accent,
                        enabled = enabled,
                        onClick = {
                            if (currentUri != null) {
                                onBrowse(currentUri, category.name)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
