package com.ultimate.filemanager.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ultimate.filemanager.core.storage.FileOperations
import com.ultimate.filemanager.core.storage.StorageAccessManager
import com.ultimate.filemanager.ui.components.AccentListCard
import com.ultimate.filemanager.ui.components.FileCategoryCard
import com.ultimate.filemanager.ui.components.ModernActionButton
import com.ultimate.filemanager.ui.components.SectionHeader
import com.ultimate.filemanager.ui.components.StorageOverviewCard
import com.ultimate.filemanager.ui.theme.CategoryColors

private data class FileCategory(
    val name: String,
    val icon: ImageVector,
    val accent: androidx.compose.ui.graphics.Color,
    val publicDir: String? = null
)

private fun hasFullStorageAccess(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onBrowse: (treeUri: String, title: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // "All files access" (Android 11+) — the real, root-level path.
    var hasFullAccess by remember { mutableStateOf(hasFullStorageAccess()) }

    // Legacy per-folder SAF access — used as a fallback below API 30.
    var grantedUri by rememberSaveable {
        mutableStateOf<String?>(
            context.contentResolver.persistedUriPermissions
                .firstOrNull()
                ?.uri
                ?.toString()
        )
    }

    // Re-check permission state when returning from the Settings screen.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasFullAccess = hasFullStorageAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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

    fun requestFullAccess() {
        val intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        runCatching { context.startActivity(intent) }
            .onFailure {
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
    }

    fun browseRoot(title: String, path: String, publicDir: String? = null) {
        val fullPath = if (publicDir != null) "$path/$publicDir" else path
        onBrowse("$ROOT_FILE_PREFIX$fullPath", title)
    }

    val storageVolumes = remember(hasFullAccess) {
        if (hasFullAccess) StorageAccessManager(context).listStorageVolumes() else emptyList()
    }

    val primaryVolumePath = remember(storageVolumes) {
        storageVolumes.firstOrNull { it.isPrimary }?.path
            ?: Environment.getExternalStorageDirectory().absolutePath
    }

    val categories = listOf(
        FileCategory("Documents", Icons.Outlined.Description, CategoryColors.Documents, Environment.DIRECTORY_DOCUMENTS),
        FileCategory("Images", Icons.Outlined.Image, CategoryColors.Images, Environment.DIRECTORY_PICTURES),
        FileCategory("Videos", Icons.Outlined.Movie, CategoryColors.Videos, Environment.DIRECTORY_MOVIES),
        FileCategory("Music", Icons.Outlined.MusicNote, CategoryColors.Music, Environment.DIRECTORY_MUSIC),
        FileCategory("Archives", Icons.Outlined.Inventory2, CategoryColors.Archives),
        FileCategory("Downloads", Icons.Outlined.Download, CategoryColors.Downloads, Environment.DIRECTORY_DOWNLOADS),
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

            when {
                hasFullAccess -> {
                    ModernActionButton(
                        text = "Browse Internal Storage",
                        icon = Icons.Outlined.FolderOpen,
                        onClick = { browseRoot("Internal Storage", primaryVolumePath) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    // Android 11+: guide the user to the real, root-level
                    // permission instead of a single-folder SAF grant.
                    ModernActionButton(
                        text = "Grant Full Storage Access",
                        icon = Icons.Outlined.FolderOpen,
                        onClick = { requestFullAccess() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You'll be taken to Settings to allow access to all files. This lets you browse the entire internal storage, not just one folder.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                currentUri != null -> {
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
                }

                else -> {
                    ModernActionButton(
                        text = "Grant Folder Access",
                        icon = Icons.Outlined.FolderOpen,
                        onClick = { folderPicker.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (hasFullAccess && storageVolumes.size > 1) {
                SectionHeader(title = "Storage")

                Spacer(modifier = Modifier.height(10.dp))

                storageVolumes.forEach { volume ->
                    AccentListCard(
                        title = volume.label,
                        description = volume.path,
                        icon = if (volume.isPrimary) Icons.Outlined.Smartphone
                        else Icons.Outlined.SdStorage,
                        accentColor = if (volume.isPrimary) CategoryColors.Folder
                        else CategoryColors.Downloads,
                        onClick = { browseRoot(volume.label, volume.path) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

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
                    val enabled = hasFullAccess || currentUri != null

                    FileCategoryCard(
                        name = category.name,
                        icon = category.icon,
                        accentColor = category.accent,
                        enabled = enabled,
                        onClick = {
                            when {
                                hasFullAccess -> browseRoot(category.name, primaryVolumePath, category.publicDir)
                                currentUri != null -> onBrowse(currentUri, category.name)
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
