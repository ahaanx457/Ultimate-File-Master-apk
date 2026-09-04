package com.ultimate.filemanager.core.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import androidx.documentfile.provider.DocumentFile

data class StorageStats(
    val totalBytes: Long,
    val freeBytes: Long
) {
    val usedBytes: Long get() = (totalBytes - freeBytes).coerceAtLeast(0L)

    val usedFraction: Float
        get() = if (totalBytes <= 0) 0f
        else (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
}

class StorageAccessManager(
    private val context: Context
) {

    fun getInternalStorage(): StorageLocation {

        return StorageLocation(

            id = "internal",

            displayName =
                "Internal storage",

            path =
                Environment
                    .getExternalStorageDirectory()
                    .absolutePath,

            type =
                StorageType.INTERNAL,

            isRemovable = false
        )
    }

    /**
     * Real, device-reported storage usage for internal shared storage via
     * StatFs. No permission is required for this — it is not per-app
     * quota, it is the actual block-device usage the OS exposes.
     */
    fun getInternalStorageStats(): StorageStats {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return StorageStats(totalBytes = total, freeBytes = free)
    }

    fun fromTreeUri(
        uriString: String
    ): StorageLocation? {

        val uri =
            runCatching {
                Uri.parse(uriString)
            }.getOrNull()
                ?: return null

        val tree =
            DocumentFile
                .fromTreeUri(
                    context,
                    uri
                )
                ?: return null

        return StorageLocation(

            id = uriString,

            displayName =
                tree.name
                    ?: "Granted Folder",

            path = null,

            type =
                StorageType.SAF_TREE,

            isRemovable = false
        )
    }
}
