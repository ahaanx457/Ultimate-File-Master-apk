package com.ultimate.filemanager.ui.screens.viewer

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ultimate.filemanager.core.storage.FileOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

private data class ZipEntryInfo(
    val path: String,
    val size: Long,
    val isDirectory: Boolean
)

@Composable
fun ArchiveViewerScreen(
    uri: Uri,
    archiveDoc: DocumentFile,
    parentDir: DocumentFile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var entries by remember(uri) { mutableStateOf<List<ZipEntryInfo>>(emptyList()) }
    var isLoading by remember(uri) { mutableStateOf(true) }
    var isExtracting by remember { mutableStateOf(false) }
    var extractDone by remember { mutableStateOf(false) }
    var extractFailed by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        isLoading = true
        entries = withContext(Dispatchers.IO) {
            runCatching {
                val list = mutableListOf<ZipEntryInfo>()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    ZipInputStream(input).use { zip ->
                        var entry: ZipEntry? = zip.nextEntry
                        while (entry != null) {
                            list.add(ZipEntryInfo(entry.name, entry.size, entry.isDirectory))
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                }
                list
            }.getOrDefault(emptyList())
        }
        isLoading = false
    }

    fun extractAll() {
        scope.launch {
            isExtracting = true
            extractDone = false
            extractFailed = false

            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val baseName = (archiveDoc.name ?: "archive").substringBeforeLast('.')
                    val destRoot = parentDir.findFile(baseName)
                        ?.takeIf { it.isDirectory }
                        ?: parentDir.createDirectory(baseName)
                        ?: return@runCatching false

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        ZipInputStream(input).use { zip ->
                            var entry: ZipEntry? = zip.nextEntry
                            while (entry != null) {
                                val segments = entry.name.trim('/').split('/')
                                var currentDir = destRoot

                                for (i in 0 until segments.size - 1) {
                                    val segmentName = segments[i]
                                    currentDir = currentDir.findFile(segmentName)
                                        ?.takeIf { it.isDirectory }
                                        ?: currentDir.createDirectory(segmentName)
                                        ?: return@runCatching false
                                }

                                if (!entry.isDirectory && segments.isNotEmpty()) {
                                    val fileName = segments.last()
                                    val newFile = currentDir.createFile(
                                        "application/octet-stream",
                                        fileName
                                    ) ?: return@runCatching false

                                    context.contentResolver.openOutputStream(newFile.uri)
                                        ?.use { out -> zip.copyTo(out) }
                                }

                                zip.closeEntry()
                                entry = zip.nextEntry
                            }
                        }
                    }
                    true
                }.getOrDefault(false)
            }

            isExtracting = false
            extractDone = success
            extractFailed = !success
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${entries.size} items",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = { extractAll() },
                enabled = !isExtracting && entries.isNotEmpty()
            ) {
                Text(if (isExtracting) "Extracting…" else "Extract All")
            }
        }

        if (extractDone) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Extracted successfully", color = MaterialTheme.colorScheme.primary)
        }
        if (extractFailed) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Extraction failed", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()

                entries.isEmpty() -> Text(
                    "This archive appears to be empty or unreadable",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> {
                    LazyColumn {
                        items(entries) { entry ->
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        if (entry.isDirectory) Icons.Outlined.Folder
                                        else Icons.Outlined.InsertDriveFile,
                                        contentDescription = null
                                    )
                                },
                                headlineContent = { Text(entry.path, maxLines = 1) },
                                supportingContent = {
                                    if (!entry.isDirectory) {
                                        Text(FileOperations.formatSize(entry.size))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
