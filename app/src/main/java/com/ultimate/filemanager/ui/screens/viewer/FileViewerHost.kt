package com.ultimate.filemanager.ui.screens.viewer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.documentfile.provider.DocumentFile
import com.ultimate.filemanager.core.storage.FileEntry

/**
 * Whether this file can be opened inside the app's own viewers rather
 * than handed off to an external app via ACTION_VIEW.
 */
fun isInAppViewable(entry: FileEntry): Boolean {
    if (entry.isDirectory) return false
    val mime = entry.mimeType ?: return false
    return mime.startsWith("image/") ||
        mime.startsWith("video/") ||
        mime == "application/pdf" ||
        mime.contains("zip")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerHost(
    entry: FileEntry,
    parentDir: DocumentFile,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mime = entry.mimeType ?: ""

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(entry.name, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when {
            mime.startsWith("image/") ->
                ImageViewerScreen(
                    uri = entry.doc.uri,
                    modifier = Modifier.weight(1f)
                )

            mime.startsWith("video/") ->
                VideoViewerScreen(
                    uri = entry.doc.uri,
                    modifier = Modifier.weight(1f)
                )

            mime == "application/pdf" ->
                PdfViewerScreen(
                    uri = entry.doc.uri,
                    modifier = Modifier.weight(1f)
                )

            mime.contains("zip") ->
                ArchiveViewerScreen(
                    uri = entry.doc.uri,
                    archiveDoc = entry.doc,
                    parentDir = parentDir,
                    modifier = Modifier.weight(1f)
                )
        }
    }
}
