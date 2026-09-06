package com.ultimate.filemanager.ui.screens

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ultimate.filemanager.core.storage.FileEntry
import com.ultimate.filemanager.core.storage.hasFullStorageAccess
import com.ultimate.filemanager.core.storage.toFileEntry
import com.ultimate.filemanager.core.storage.walkFiles
import com.ultimate.filemanager.ui.components.EmptyState
import com.ultimate.filemanager.ui.screens.viewer.FileViewerHost
import com.ultimate.filemanager.ui.screens.viewer.isInAppViewable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<FileEntry>()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var viewerEntry by remember { mutableStateOf<FileEntry?>(null) }

    fun runSearch() {
        val term = query.trim()
        if (term.isEmpty()) return

        searchJob?.cancel()

        results = emptyList()
        isSearching = true

        searchJob = scope.launch {
            val root = DocumentFile.fromFile(Environment.getExternalStorageDirectory())

            try {
                withContext(Dispatchers.IO) {
                    walkFiles(root) { doc ->
                        val name = doc.name ?: return@walkFiles
                        if (name.contains(term, ignoreCase = true)) {
                            doc.toFileEntry()?.let { entry ->
                                withContext(Dispatchers.Main) {
                                    results = results + entry
                                }
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                // Stopped by the user — keep whatever was already found.
            } finally {
                isSearching = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = { Text("Search files by name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSearching) {
                        IconButton(onClick = { searchJob?.cancel() }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Stop")
                        }
                    } else {
                        IconButton(onClick = { runSearch() }, enabled = query.isNotBlank()) {
                            Icon(Icons.Outlined.Search, contentDescription = "Search")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                !hasFullStorageAccess() -> {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = "Full storage access needed",
                        description = "Grant full storage access from Home to search across your device.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                isSearching && results.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Searching…")
                    }
                }

                results.isEmpty() && query.isNotBlank() && !isSearching -> {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = "No matches found",
                        description = "Nothing on your device matched \"$query\".",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                results.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = "Search your files",
                        description = "Type a file name above and tap search.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (isSearching) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Text(
                            "${results.size} result${if (results.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(results, key = { it.doc.uri.toString() }) { entry ->
                                ListItem(
                                    headlineContent = { Text(entry.name, maxLines = 1) },
                                    supportingContent = {
                                        Text(
                                            entry.doc.uri.path ?: "",
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        if (isInAppViewable(entry)) {
                                            viewerEntry = entry
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            viewerEntry?.let { entry ->
                val parentPath = java.io.File(entry.doc.uri.path ?: "").parentFile
                    ?: Environment.getExternalStorageDirectory()
                val parent = DocumentFile.fromFile(parentPath)

                FileViewerHost(
                    entry = entry,
                    parentDir = parent,
                    onClose = { viewerEntry = null },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
