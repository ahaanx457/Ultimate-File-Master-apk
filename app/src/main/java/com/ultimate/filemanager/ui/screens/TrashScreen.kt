package com.ultimate.filemanager.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ultimate.filemanager.core.storage.FileOperations
import com.ultimate.filemanager.core.trash.TrashItem
import com.ultimate.filemanager.core.trash.TrashStore
import com.ultimate.filemanager.ui.components.EmptyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<TrashItem>>(emptyList()) }
    var forgetTarget by remember { mutableStateOf<TrashItem?>(null) }
    var confirmEmptyAll by remember { mutableStateOf(false) }
    var restoreFailedFor by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        items = TrashStore.listItems(context)
    }

    LaunchedEffect(Unit) { refresh() }

    val totalSize = remember(items) { items.sumOf { it.size } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trash") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = { confirmEmptyAll = true }) {
                            Text("Empty")
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
            if (items.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Delete,
                    title = "Trash is empty",
                    description = "Files you delete from the browser show up here before they're gone for good.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    "${items.size} item${if (items.size == 1) "" else "s"} · ${FileOperations.formatSize(totalSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )

                if (restoreFailedFor != null) {
                    Text(
                        "Couldn't restore \"$restoreFailedFor\" — its original folder may no longer exist.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.originalName, maxLines = 1) },
                            supportingContent = {
                                Text(
                                    "${FileOperations.formatSize(item.size)} · deleted " +
                                        DateFormat.format("dd MMM yyyy", Date(item.dateTrashed))
                                )
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        scope.launch {
                                            val success = withContext(Dispatchers.IO) {
                                                TrashStore.restore(context, item)
                                            }
                                            restoreFailedFor = if (success) null else item.originalName
                                            refresh()
                                        }
                                    }) {
                                        Icon(Icons.Outlined.Restore, contentDescription = "Restore")
                                    }
                                    IconButton(onClick = { forgetTarget = item }) {
                                        Icon(Icons.Outlined.DeleteForever, contentDescription = "Delete forever")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    forgetTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { forgetTarget = null },
            title = { Text("Delete forever?") },
            text = { Text("\"${item.originalName}\" will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    TrashStore.deleteForever(context, item)
                    forgetTarget = null
                    refresh()
                }) { Text("Delete Forever") }
            },
            dismissButton = {
                TextButton(onClick = { forgetTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (confirmEmptyAll) {
        AlertDialog(
            onDismissRequest = { confirmEmptyAll = false },
            title = { Text("Empty trash?") },
            text = { Text("All ${items.size} items will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    TrashStore.emptyTrash(context)
                    confirmEmptyAll = false
                    refresh()
                }) { Text("Empty Trash") }
            },
            dismissButton = {
                TextButton(onClick = { confirmEmptyAll = false }) { Text("Cancel") }
            }
        )
    }
}
