package com.ultimate.filemanager.ui.screens

import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ultimate.filemanager.core.storage.FileOperations
import com.ultimate.filemanager.core.storage.hasFullStorageAccess
import com.ultimate.filemanager.core.storage.walkFiles
import com.ultimate.filemanager.ui.components.EmptyState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

private data class DupFile(
    val doc: DocumentFile,
    val name: String,
    val size: Long
)

private data class DupGroup(
    val hash: String,
    val size: Long,
    val files: List<DupFile>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateFinderScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scanJob by remember { mutableStateOf<Job?>(null) }
    var groups by remember { mutableStateOf<List<DupGroup>>(emptyList()) }
    var selected by remember { mutableStateOf(setOf<android.net.Uri>()) }
    var isDeleting by remember { mutableStateOf(false) }
    var scannedCount by remember { mutableIntStateOf(0) }

    fun hashFile(doc: DocumentFile): String? {
        return runCatching {
            val digest = MessageDigest.getInstance("MD5")
            context.contentResolver.openInputStream(doc.uri)?.use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }.getOrNull()
    }

    fun runScan() {
        scanJob?.cancel()
        groups = emptyList()
        selected = emptySet()
        scannedCount = 0
        isScanning = true

        scanJob = scope.launch {
            try {
                val allFiles = mutableListOf<DupFile>()

                withContext(Dispatchers.IO) {
                    val root = DocumentFile.fromFile(Environment.getExternalStorageDirectory())
                    walkFiles(root) { doc ->
                        val name = doc.name ?: return@walkFiles
                        val size = doc.length()
                        if (size > 0) {
                            allFiles.add(DupFile(doc, name, size))
                            scannedCount = allFiles.size
                        }
                    }
                }

                // Only files sharing a size with at least one other file
                // are worth hashing — this avoids hashing the whole device.
                val bySize = allFiles.groupBy { it.size }.filterValues { it.size > 1 }

                val result = withContext(Dispatchers.IO) {
                    val byHash = mutableMapOf<String, MutableList<DupFile>>()

                    bySize.values.forEach { candidates ->
                        candidates.forEach { file ->
                            val hash = hashFile(file.doc) ?: return@forEach
                            val key = "${file.size}_$hash"
                            byHash.getOrPut(key) { mutableListOf() }.add(file)
                        }
                    }

                    byHash.values
                        .filter { it.size > 1 }
                        .map { files ->
                            DupGroup(
                                hash = files.first().let { "${it.size}" },
                                size = files.first().size,
                                files = files
                            )
                        }
                        .sortedByDescending { it.size * (it.files.size - 1) }
                }

                groups = result

                // Pre-select all but one file in each group (keep the first).
                selected = result.flatMap { group ->
                    group.files.drop(1).map { it.doc.uri }
                }.toSet()

            } catch (e: CancellationException) {
                // Stopped by the user.
            } finally {
                isScanning = false
            }
        }
    }

    val wastedBytes = groups.sumOf { it.size * (it.files.size - 1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duplicate Finder") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isScanning) {
                        TextButton(onClick = { scanJob?.cancel() }) {
                            Text("Stop")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                !hasFullStorageAccess() -> {
                    EmptyState(
                        icon = Icons.Outlined.ContentCopy,
                        title = "Full storage access needed",
                        description = "Grant full storage access from Home to scan for duplicates.",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                groups.isEmpty() && !isScanning -> {
                    EmptyState(
                        icon = Icons.Outlined.ContentCopy,
                        title = "Find duplicate files",
                        description = "Scan your storage for files with identical content and free up space.",
                        actionLabel = "Scan Now",
                        onAction = { runScan() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    if (isScanning) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            "Scanned $scannedCount files…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (groups.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${groups.size} duplicate groups",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${FileOperations.formatSize(wastedBytes)} can be freed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                enabled = selected.isNotEmpty() && !isDeleting,
                                onClick = {
                                    scope.launch {
                                        isDeleting = true
                                        val toDelete = groups
                                            .flatMap { it.files }
                                            .filter { it.doc.uri in selected }

                                        withContext(Dispatchers.IO) {
                                            toDelete.forEach { it.doc.delete() }
                                        }

                                        groups = groups.mapNotNull { group ->
                                            val remaining = group.files.filter { it.doc.uri !in selected }
                                            if (remaining.size > 1) group.copy(files = remaining) else null
                                        }
                                        selected = emptySet()
                                        isDeleting = false
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isDeleting) "Deleting…" else "Delete Selected")
                            }
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(groups) { group ->
                                Text(
                                    "${FileOperations.formatSize(group.size)} · ${group.files.size} copies",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                )

                                group.files.forEach { file ->
                                    ListItem(
                                        headlineContent = { Text(file.name, maxLines = 1) },
                                        supportingContent = {
                                            Text(file.doc.uri.path ?: "", maxLines = 1)
                                        },
                                        leadingContent = {
                                            Checkbox(
                                                checked = file.doc.uri in selected,
                                                onCheckedChange = { checked ->
                                                    selected = if (checked) {
                                                        selected + file.doc.uri
                                                    } else {
                                                        selected - file.doc.uri
                                                    }
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
