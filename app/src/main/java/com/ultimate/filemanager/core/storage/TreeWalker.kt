package com.ultimate.filemanager.core.storage

import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Recursively visits every file (not folder) under [root], depth-first.
 * Folders that fail to list (permission issues, broken providers) are
 * skipped rather than aborting the whole walk. Respects coroutine
 * cancellation so a screen can stop an in-progress scan cleanly.
 */
suspend fun walkFiles(
    root: DocumentFile,
    onFile: suspend (DocumentFile) -> Unit
) {
    val children = runCatching { root.listFiles() }.getOrNull() ?: return

    for (child in children) {
        currentCoroutineContext().ensureActive()

        if (child.isDirectory) {
            walkFiles(child, onFile)
        } else if (child.isFile) {
            onFile(child)
        }
    }
}
