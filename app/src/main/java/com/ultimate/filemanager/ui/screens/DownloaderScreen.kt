package com.ultimate.filemanager.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ultimate.filemanager.core.storage.FileOperations
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class DownloadStatus { PENDING, RUNNING, SUCCESSFUL, FAILED }

private data class ActiveDownload(
    val id: Long,
    val fileName: String,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val bytesDownloaded: Long = 0L,
    val bytesTotal: Long = 0L
)

private fun queryDownload(context: Context, id: Long): Triple<DownloadStatus, Long, Long>? {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        ?: return null

    val cursor = manager.query(DownloadManager.Query().setFilterById(id)) ?: return null

    cursor.use {
        if (!it.moveToFirst()) return null

        val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val downloadedIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        val totalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

        val status = when (it.getInt(statusIndex)) {
            DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
            DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
            DownloadManager.STATUS_RUNNING -> DownloadStatus.RUNNING
            else -> DownloadStatus.PENDING
        }

        val downloaded = if (downloadedIndex >= 0) it.getLong(downloadedIndex) else 0L
        val total = if (totalIndex >= 0) it.getLong(totalIndex) else 0L

        return Triple(status, downloaded, total)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var url by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var downloads by remember { mutableStateOf(listOf<ActiveDownload>()) }

    fun startDownload() {
        error = null
        val trimmedUrl = url.trim()

        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            error = "Enter a valid http(s) link"
            return
        }

        val parsedUri = runCatching { Uri.parse(trimmedUrl) }.getOrNull()
        if (parsedUri == null) {
            error = "Couldn't parse that link"
            return
        }

        val resolvedName = fileName.trim().ifBlank {
            parsedUri.lastPathSegment?.takeIf { it.isNotBlank() } ?: "download_${System.currentTimeMillis()}"
        }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (manager == null) {
            error = "Download service is unavailable"
            return
        }

        val request = DownloadManager.Request(parsedUri)
            .setTitle(resolvedName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, resolvedName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val id = runCatching { manager.enqueue(request) }.getOrNull()
        if (id == null) {
            error = "Couldn't start the download"
            return
        }

        downloads = downloads + ActiveDownload(id = id, fileName = resolvedName)
        url = ""
        fileName = ""

        scope.launch {
            while (true) {
                val result = queryDownload(context, id) ?: break
                val (status, downloaded, total) = result

                downloads = downloads.map {
                    if (it.id == id) it.copy(status = status, bytesDownloaded = downloaded, bytesTotal = total) else it
                }

                if (status == DownloadStatus.SUCCESSFUL || status == DownloadStatus.FAILED) break
                delay(500)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloader") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Direct file link (https://...)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("File name (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { startDownload() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download to Downloads folder")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (downloads.isNotEmpty()) {
                Text("This session", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(downloads.reversed(), key = { it.id }) { download ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(download.fileName, maxLines = 1, modifier = Modifier.weight(1f))
                                Text(
                                    when (download.status) {
                                        DownloadStatus.SUCCESSFUL -> "Done"
                                        DownloadStatus.FAILED -> "Failed"
                                        DownloadStatus.RUNNING -> "Downloading"
                                        DownloadStatus.PENDING -> "Waiting"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (download.status) {
                                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                                        DownloadStatus.SUCCESSFUL -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            if (download.status == DownloadStatus.RUNNING && download.bytesTotal > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        (download.bytesDownloaded.toFloat() / download.bytesTotal.toFloat())
                                            .coerceIn(0f, 1f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "${FileOperations.formatSize(download.bytesDownloaded)} / " +
                                        FileOperations.formatSize(download.bytesTotal),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
