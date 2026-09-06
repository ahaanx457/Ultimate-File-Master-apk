package com.ultimate.filemanager.ui.screens

import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ultimate.filemanager.core.storage.FileEntry
import com.ultimate.filemanager.core.storage.FileOperations
import com.ultimate.filemanager.core.vault.VaultAuth
import com.ultimate.filemanager.core.vault.VaultItem
import com.ultimate.filemanager.core.vault.VaultStore
import com.ultimate.filemanager.ui.components.EmptyState
import com.ultimate.filemanager.ui.screens.viewer.FileViewerHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

private enum class VaultMode { SET_PIN, CONFIRM_PIN, ENTER_PIN, UNLOCKED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mode by remember {
        mutableStateOf(if (VaultAuth.isPinSet(context)) VaultMode.ENTER_PIN else VaultMode.SET_PIN)
    }

    var pinInput by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var items by remember { mutableStateOf<List<VaultItem>>(emptyList()) }
    var deleteTarget by remember { mutableStateOf<VaultItem?>(null) }
    var viewerEntry by remember { mutableStateOf<Pair<VaultItem, FileEntry>?>(null) }
    var viewerTempFile by remember { mutableStateOf<File?>(null) }
    var restoreTarget by remember { mutableStateOf<VaultItem?>(null) }

    fun refreshItems() {
        items = VaultStore.listItems(context)
    }

    LaunchedEffect(mode) {
        if (mode == VaultMode.UNLOCKED) refreshItems()
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { destinationUri ->
        val target = restoreTarget
        restoreTarget = null
        if (destinationUri == null || target == null) return@rememberLauncherForActivityResult

        scope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = VaultStore.readItemBytes(context, target.id)
                        ?: return@runCatching false
                    context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                        out.write(bytes)
                    }
                    true
                }.getOrDefault(false)
            }
            if (success) {
                VaultStore.deleteItem(context, target.id)
                refreshItems()
            }
        }
    }

    fun openItem(item: VaultItem) {
        scope.launch {
            val tempFile = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = VaultStore.readItemBytes(context, item.id) ?: return@runCatching null
                    val dir = File(context.cacheDir, "vault_temp").apply { mkdirs() }
                    val file = File(dir, item.originalName)
                    file.writeBytes(bytes)
                    file
                }.getOrNull()
            }

            if (tempFile != null) {
                val doc = DocumentFile.fromFile(tempFile)
                val entry = FileEntry(
                    doc = doc,
                    name = item.originalName,
                    isDirectory = false,
                    size = item.size,
                    lastModified = item.dateAdded,
                    mimeType = item.mimeType
                )
                viewerTempFile = tempFile
                viewerEntry = item to entry
            }
        }
    }

    fun closeViewer() {
        viewerTempFile?.delete()
        viewerTempFile = null
        viewerEntry = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
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
            when (mode) {
                VaultMode.SET_PIN, VaultMode.CONFIRM_PIN -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (mode == VaultMode.SET_PIN) "Create a Vault PIN"
                            else "Confirm your PIN",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "You'll need this PIN every time you open the Vault.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 6) pinInput = it.filter(Char::isDigit) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError,
                            label = { Text("4-6 digit PIN") }
                        )
                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PINs don't match. Try again.", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (mode == VaultMode.SET_PIN) {
                                    if (pinInput.length >= 4) {
                                        firstPin = pinInput
                                        pinInput = ""
                                        pinError = false
                                        mode = VaultMode.CONFIRM_PIN
                                    }
                                } else {
                                    if (pinInput == firstPin) {
                                        VaultAuth.setPin(context, pinInput)
                                        pinInput = ""
                                        mode = VaultMode.UNLOCKED
                                    } else {
                                        pinError = true
                                        pinInput = ""
                                    }
                                }
                            },
                            enabled = pinInput.length >= 4
                        ) {
                            Text(if (mode == VaultMode.SET_PIN) "Next" else "Confirm")
                        }
                    }
                }

                VaultMode.ENTER_PIN -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Enter your Vault PIN", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 6) pinInput = it.filter(Char::isDigit) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError,
                            label = { Text("PIN") }
                        )
                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Incorrect PIN", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (VaultAuth.verifyPin(context, pinInput)) {
                                    pinError = false
                                    pinInput = ""
                                    mode = VaultMode.UNLOCKED
                                } else {
                                    pinError = true
                                }
                            },
                            enabled = pinInput.length >= 4
                        ) {
                            Text("Unlock")
                        }
                    }
                }

                VaultMode.UNLOCKED -> {
                    if (items.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.Lock,
                            title = "Vault is empty",
                            description = "Add files to the Vault from the file browser's item menu.",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items, key = { it.id }) { item ->
                                ListItem(
                                    headlineContent = { Text(item.originalName, maxLines = 1) },
                                    supportingContent = {
                                        Text(
                                            "${FileOperations.formatSize(item.size)} · " +
                                                DateFormat.format("dd MMM yyyy", Date(item.dateAdded))
                                        )
                                    },
                                    leadingContent = {
                                        Icon(Icons.Outlined.Lock, contentDescription = null)
                                    },
                                    trailingContent = {
                                        Row {
                                            IconButton(onClick = {
                                                restoreTarget = item
                                                restoreLauncher.launch(item.originalName)
                                            }) {
                                                Icon(Icons.Outlined.Restore, contentDescription = "Restore")
                                            }
                                            IconButton(onClick = { deleteTarget = item }) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable { openItem(item) }
                                )
                            }
                        }
                    }
                }
            }

            viewerEntry?.let { (_, entry) ->
                FileViewerHost(
                    entry = entry,
                    parentDir = DocumentFile.fromFile(File(context.cacheDir, "vault_temp")),
                    onClose = { closeViewer() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete from Vault?") },
            text = { Text("\"${item.originalName}\" will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    VaultStore.deleteItem(context, item.id)
                    deleteTarget = null
                    refreshItems()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}
