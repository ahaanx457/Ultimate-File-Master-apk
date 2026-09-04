package com.ultimate.filemanager.core.storage

import android.content.Context
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * All operations here work purely through the Storage Access Framework
 * (DocumentFile / DocumentsContract), so they only ever touch folders the
 * user has explicitly granted access to.
 */
object FileOperations {

    fun rename(doc: DocumentFile, newName: String): Boolean {
        return doc.renameTo(newName)
    }

    fun delete(doc: DocumentFile): Boolean {
        return doc.delete()
    }

    fun createFolder(parent: DocumentFile, name: String): DocumentFile? {
        return parent.createDirectory(name)
    }

    suspend fun copy(
        context: Context,
        source: DocumentFile,
        destinationParent: DocumentFile
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            copyRecursive(context, source, destinationParent)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun move(
        context: Context,
        source: DocumentFile,
        sourceParent: DocumentFile,
        destinationParent: DocumentFile
    ): Boolean = withContext(Dispatchers.IO) {
        // Try the fast native move first (works when source and destination
        // are on the same underlying document provider / tree).
        try {
            val moved = DocumentsContract.moveDocument(
                context.contentResolver,
                source.uri,
                sourceParent.uri,
                destinationParent.uri
            )
            if (moved != null) return@withContext true
        } catch (ignored: Exception) {
            // Fall through to copy + delete below.
        }

        return@withContext try {
            copyRecursive(context, source, destinationParent)
            source.delete()
        } catch (e: Exception) {
            false
        }
    }

    private fun copyRecursive(
        context: Context,
        source: DocumentFile,
        destinationParent: DocumentFile
    ) {
        val name = source.name ?: return

        if (source.isDirectory) {
            val newDir = destinationParent.findFile(name)
                ?.takeIf { it.isDirectory }
                ?: destinationParent.createDirectory(name)
                ?: throw IllegalStateException("Could not create folder $name")

            source.listFiles().forEach { child ->
                copyRecursive(context, child, newDir)
            }
        } else {
            val mimeType = source.type ?: "application/octet-stream"
            val newFile = destinationParent.createFile(mimeType, name)
                ?: throw IllegalStateException("Could not create file $name")

            context.contentResolver.openInputStream(source.uri)?.use { input ->
                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "${value.toInt()} ${units[unitIndex]}"
        } else {
            "%.1f %s".format(value, units[unitIndex])
        }
    }
}
