package com.ultimate.filemanager.ui.screens

import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ultimate.filemanager.core.storage.FileEntry
import com.ultimate.filemanager.core.storage.FileOperations
import com.ultimate.filemanager.core.storage.SortDirection
import com.ultimate.filemanager.core.storage.SortField
import com.ultimate.filemanager.core.storage.sortedWith
import com.ultimate.filemanager.core.storage.toFileEntry
import com.ultimate.filemanager.ui.components.EmptyState
import com.ultimate.filemanager.ui.theme.CategoryColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileBrowserScreen(
    rootTreeUri: String,
    title: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val rootDoc = remember(rootTreeUri) {
        if (rootTreeUri.startsWith(ROOT_FILE_PREFIX)) {
            // Full "All files access" mode: browse the real filesystem
            // directly. DocumentFile.fromFile() exposes the exact same
            // API (listFiles/createFile/delete/renameTo/...), so every
            // other operation in this screen works unchanged.
            DocumentFile.fromFile(java.io.File(rootTreeUri.removePrefix(ROOT_FILE_PREFIX)))
        } else {
            DocumentFile.fromTreeUri(context, Uri.parse(rootTreeUri))
        }
    }

    var pathStack by remember {
        mutableStateOf(listOfNotNull(rootDoc))
    }
    val currentDir = pathStack.lastOrNull()

    var entries by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var searchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    var sortField by remember { mutableStateOf(SortField.NAME) }
    var sortDirection by remember { mutableStateOf(SortDirection.ASCENDING) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    var selectedUris by remember { mutableStateOf(setOf<Uri>()) }
    val selectionMode = selectedUris.isNotEmpty()

    var itemMenuTarget by remember { mutableStateOf<FileEntry?>(null) }
    var renameTarget by remember { mutableStateOf<FileEntry?>(null) }
    var detailsTarget by remember { mutableStateOf<FileEntry?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }

    var pendingBulkAction by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        val dir = currentDir ?: return
        isLoading = true
        scope.launch {
            val children = withContext(Dispatchers.IO) {
                dir.listFiles().mapNotNull { it.toFileEntry() }
            }
            entries = children
            isLoading = false
        }
    }

    LaunchedEffect(currentDir) {
        refresh()
        selectedUris = emptySet()
    }

    val destinationPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { pickedUri ->
        val action = pendingBulkAction
        pendingBulkAction = null
        if (pickedUri == null || action == null) return@rememberLauncherForActivityResult

        context.contentResolver.takePersistableUriPermission(
            pickedUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        val destination = DocumentFile.fromTreeUri(context, pickedUri)
        val targets = entries.filter { it.doc.uri in selectedUris }
        val parent = currentDir

        if (destination == null || parent == null) return@rememberLauncherForActivityResult

        scope.launch {
            isBusy = true
            targets.forEach { entry ->
                if (action == "move") {
                    FileOperations.move(context, entry.doc, parent, destination)
                } else {
                    FileOperations.copy(context, entry.doc, destination)
                }
            }
            isBusy = false
            selectedUris = emptySet()
            refresh()
        }
    }

    fun openFile(entry: FileEntry) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(entry.doc.uri, entry.mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                runCatching {
                    context.startActivity(Intent.createChooser(intent, "Open with"))
                }
            }
    }

    val visibleEntries = remember(entries, query, sortField, sortDirection) {
        val filtered = if (query.isBlank()) {
            entries
        } else {
            entries.filter { it.name.contains(query, ignoreCase = true) }
        }
        filtered.sortedWith(sortField, sortDirection)
    }

    BackHandler(enabled = pathStack.size > 1) {
        pathStack = pathStack.dropLast(1)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (searchActive) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                placeholder = { Text("Search in this folder") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (selectionMode) {
                            Text("${selectedUris.size} selected")
                        } else {
                            Text(
                                pathStack.lastOrNull()?.name ?: title,
                                maxLines = 1
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                when {
                                    searchActive -> {
                                        searchActive = false
                                        query = ""
                                    }
                                    selectionMode -> selectedUris = emptySet()
                                    pathStack.size > 1 -> pathStack = pathStack.dropLast(1)
                                    else -> onExit()
                                }
                            }
                        ) {
                            Icon(
                                if (selectionMode) Icons.Outlined.Close
                                else Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (selectionMode) {
                            IconButton(onClick = {
                                pendingBulkAction = "copy"
                                destinationPicker.launch(null)
                            }) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy")
                            }
                            IconButton(onClick = {
                                pendingBulkAction = "move"
                                destinationPicker.launch(null)
                            }) {
                                Icon(Icons.Outlined.DriveFileMove, contentDescription = "Move")
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    isBusy = true
                                    entries.filter { it.doc.uri in selectedUris }
                                        .forEach { FileOperations.delete(it.doc) }
                                    isBusy = false
                                    selectedUris = emptySet()
                                    refresh()
                                }
                            }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                            }
                        } else {
                            IconButton(onClick = { searchActive = true }) {
                                Icon(Icons.Outlined.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { sortMenuOpen = true }) {
                                Icon(Icons.Outlined.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = sortMenuOpen,
                                onDismissRequest = { sortMenuOpen = false }
                            ) {
                                listOf(
                                    SortField.NAME to "Name",
                                    SortField.SIZE to "Size",
                                    SortField.DATE to "Date",
                                    SortField.TYPE to "Type"
                                ).forEach { (field, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            if (sortField == field) {
                                                sortDirection =
                                                    if (sortDirection == SortDirection.ASCENDING)
                                                        SortDirection.DESCENDING
                                                    else SortDirection.ASCENDING
                                            } else {
                                                sortField = field
                                                sortDirection = SortDirection.ASCENDING
                                            }
                                            sortMenuOpen = false
                                        },
                                        trailingIcon = {
                                            if (sortField == field) {
                                                Icon(
                                                    if (sortDirection == SortDirection.ASCENDING)
                                                        Icons.Outlined.ArrowUpward
                                                    else Icons.Outlined.ArrowDownward,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                )

                if (isBusy) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(onClick = { showNewFolderDialog = true }) {
                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = "New folder")
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                visibleEntries.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        EmptyState(
                            icon = if (query.isNotBlank()) Icons.Outlined.SearchOff
                            else Icons.Outlined.FolderOff,
                            title = if (query.isNotBlank()) "No matches found"
                            else "This folder is empty",
                            description = if (query.isNotBlank())
                                "Try a different search term."
                            else
                                "Files and folders you add here will show up in this list."
                        )
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(visibleEntries, key = { it.doc.uri.toString() }) { entry ->
                            FileRow(
                                entry = entry,
                                selected = entry.doc.uri in selectedUris,
                                selectionMode = selectionMode,
                                onClick = {
                                    when {
                                        selectionMode -> {
                                            selectedUris =
                                                if (entry.doc.uri in selectedUris)
                                                    selectedUris - entry.doc.uri
                                                else selectedUris + entry.doc.uri
                                        }
                                        entry.isDirectory -> {
                                            pathStack = pathStack + entry.doc
                                        }
                                        else -> openFile(entry)
                                    }
                                },
                                onLongClick = {
                                    if (!selectionMode) {
                                        selectedUris = setOf(entry.doc.uri)
                                    }
                                },
                                onMenu = { itemMenuTarget = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    itemMenuTarget?.let { entry ->
        ModalBottomSheet(onDismissRequest = { itemMenuTarget = null }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                ListItem(
                    headlineContent = { Text("Rename") },
                    leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    modifier = Modifier.combinedClickable(onClick = {
                        renameTarget = entry
                        itemMenuTarget = null
                    })
                )
                ListItem(
                    headlineContent = { Text("Delete") },
                    leadingContent = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    modifier = Modifier.combinedClickable(onClick = {
                        scope.launch {
                            FileOperations.delete(entry.doc)
                            itemMenuTarget = null
                            refresh()
                        }
                    })
                )
                ListItem(
                    headlineContent = { Text("Share") },
                    leadingContent = { Icon(Icons.Outlined.Share, contentDescription = null) },
                    modifier = Modifier.combinedClickable(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = entry.mimeType ?: "*/*"
                            putExtra(Intent.EXTRA_STREAM, entry.doc.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share"))
                        itemMenuTarget = null
                    })
                )
                ListItem(
                    headlineContent = { Text("Details") },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    modifier = Modifier.combinedClickable(onClick = {
                        detailsTarget = entry
                        itemMenuTarget = null
                    })
                )
            }
        }
    }

    renameTarget?.let { entry ->
        var newName by remember(entry) { mutableStateOf(entry.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    FileOperations.rename(entry.doc, newName)
                    renameTarget = null
                    refresh()
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    detailsTarget?.let { entry ->
        AlertDialog(
            onDismissRequest = { detailsTarget = null },
            title = { Text(entry.name) },
            text = {
                Column {
                    DetailRow("Type", if (entry.isDirectory) "Folder" else (entry.mimeType ?: "Unknown"))
                    DetailRow("Size", if (entry.isDirectory) "—" else FileOperations.formatSize(entry.size))
                    DetailRow(
                        "Modified",
                        DateFormat.format("dd MMM yyyy, hh:mm a", Date(entry.lastModified)).toString()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { detailsTarget = null }) { Text("Close") }
            }
        )
    }

    if (showNewFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New folder") },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    singleLine = true,
                    placeholder = { Text("Folder name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dir = currentDir
                        if (dir != null && folderName.isNotBlank()) {
                            FileOperations.createFolder(dir, folderName)
                            refresh()
                        }
                        showNewFolderDialog = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    entry: FileEntry,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenu: () -> Unit
) {
    val icon = when {
        entry.isDirectory -> Icons.Outlined.Folder
        entry.mimeType?.startsWith("image/") == true -> Icons.Outlined.Image
        entry.mimeType?.startsWith("video/") == true -> Icons.Outlined.Movie
        entry.mimeType?.startsWith("audio/") == true -> Icons.Outlined.MusicNote
        entry.mimeType == "application/pdf" -> Icons.Outlined.PictureAsPdf
        entry.mimeType?.contains("zip") == true -> Icons.Outlined.Inventory2
        else -> Icons.Outlined.InsertDriveFile
    }

    val accent = when {
        entry.isDirectory -> CategoryColors.Folder
        entry.mimeType?.startsWith("image/") == true -> CategoryColors.Images
        entry.mimeType?.startsWith("video/") == true -> CategoryColors.Videos
        entry.mimeType?.startsWith("audio/") == true -> CategoryColors.Music
        entry.mimeType == "application/pdf" -> CategoryColors.Documents
        entry.mimeType?.contains("zip") == true -> CategoryColors.Archives
        else -> CategoryColors.Other
    }

    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        leadingContent = {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = { onClick() })
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
            }
        },
        headlineContent = { Text(entry.name, maxLines = 1) },
        supportingContent = {
            Text(
                if (entry.isDirectory) "Folder"
                else FileOperations.formatSize(entry.size)
            )
        },
        trailingContent = {
            if (!selectionMode) {
                IconButton(onClick = onMenu) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                }
            }
        }
    )
}

/**
 * When the treeUri passed to FileBrowserScreen starts with this prefix,
 * the remainder is treated as a real filesystem path (used with the
 * "All files access" permission) instead of a SAF content:// tree URI.
 */
const val ROOT_FILE_PREFIX = "ROOT_FILE:"
