package com.ultimate.filemanager.core.storage

import androidx.documentfile.provider.DocumentFile

data class FileEntry(
    val doc: DocumentFile,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val mimeType: String?
)

enum class SortField {
    NAME, SIZE, DATE, TYPE
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

fun DocumentFile.toFileEntry(): FileEntry? {
    val docName = name ?: return null
    return FileEntry(
        doc = this,
        name = docName,
        isDirectory = isDirectory,
        size = if (isDirectory) 0L else length(),
        lastModified = lastModified(),
        mimeType = type
    )
}

fun List<FileEntry>.sortedWith(
    field: SortField,
    direction: SortDirection
): List<FileEntry> {
    // Folders always float to the top, matching how most file managers behave.
    val comparator = when (field) {
        SortField.NAME ->
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }

        SortField.SIZE ->
            compareBy<FileEntry> { it.size }

        SortField.DATE ->
            compareBy<FileEntry> { it.lastModified }

        SortField.TYPE ->
            compareBy(String.CASE_INSENSITIVE_ORDER) {
                it.mimeType ?: ""
            }
    }

    val ordered = if (direction == SortDirection.ASCENDING) {
        sortedWith(comparator)
    } else {
        sortedWith(comparator.reversed())
    }

    return ordered.sortedByDescending { it.isDirectory }
}
