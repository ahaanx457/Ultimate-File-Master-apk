package com.ultimate.filemanager.ui.screens.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ultimate.filemanager.core.storage.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** File extensions treated as editable text/code even when the OS doesn't map a text/* mime type to them. */
private val TEXT_LIKE_EXTENSIONS = setOf(
    "txt", "md", "markdown", "json", "xml", "yml", "yaml",
    "gradle", "kts", "kt", "java", "py", "js", "ts", "jsx", "tsx",
    "c", "cpp", "h", "hpp", "cs", "go", "rs", "rb", "php",
    "html", "htm", "css", "scss", "sql", "sh", "bash",
    "ini", "conf", "cfg", "properties", "log", "csv", "toml",
    "gitignore", "env"
)

fun isTextLike(entry: FileEntry): Boolean {
    if (entry.isDirectory) return false
    if (entry.mimeType?.startsWith("text/") == true) return true
    val ext = entry.name.substringAfterLast('.', "").lowercase()
    return ext in TEXT_LIKE_EXTENSIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextViewerScreen(
    entry: FileEntry,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var text by remember(entry) { mutableStateOf<String?>(null) }
    var originalText by remember(entry) { mutableStateOf<String?>(null) }
    var isLoading by remember(entry) { mutableStateOf(true) }
    var failed by remember(entry) { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }

    val currentText = text
    val hasChanges = currentText != null && currentText != originalText

    LaunchedEffect(entry) {
        isLoading = true
        val content = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(entry.doc.uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                }
            }.getOrNull()
        }
        if (content == null) {
            failed = true
        } else {
            text = content
            originalText = content
        }
        isLoading = false
    }

    fun save() {
        val toSave = text ?: return
        scope.launch {
            isSaving = true
            saveFailed = false
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(entry.doc.uri, "wt")?.use { out ->
                        out.write(toSave.toByteArray(Charsets.UTF_8))
                    }
                    true
                }.getOrDefault(false)
            }
            isSaving = false
            if (success) {
                originalText = toSave
            } else {
                saveFailed = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(entry.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasChanges) {
                        IconButton(onClick = { save() }, enabled = !isSaving) {
                            Icon(Icons.Outlined.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                failed || currentText == null -> Text(
                    "Couldn't open this file as text",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (saveFailed) {
                            Text(
                                "Save failed. Check that you still have write access to this file.",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        OutlinedTextField(
                            value = currentText,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }
    }
}
