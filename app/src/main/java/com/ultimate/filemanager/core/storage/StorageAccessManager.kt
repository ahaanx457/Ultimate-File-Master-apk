package com.ultimate.filemanager.core.storage

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
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

data class StorageVolumeInfo(
    val label: String,
    val path: String,
    val isPrimary: Boolean,
    val isRemovable: Boolean
)

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

    /**
     * Every mounted storage volume the OS knows about — internal shared
     * storage, plus any inserted SD card or connected USB/OTG drive
     * (e.g. /storage/emulated/0, /storage/1234-5678). Requires "All files
     * access" (MANAGE_EXTERNAL_STORAGE, Android 11+) to get real,
     * browsable filesystem paths for each one.
     */
    fun listStorageVolumes(): List<StorageVolumeInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return emptyList()

        val storageManager =
            context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
                ?: return emptyList()

        return storageManager.storageVolumes.mapNotNull { volume ->
            if (volume.state != Environment.MEDIA_MOUNTED) return@mapNotNull null
            val dir = volume.directory ?: return@mapNotNull null

            StorageVolumeInfo(
                label = volume.getDescription(context)
                    ?: if (volume.isPrimary) "Internal Storage" else "External Storage",
                path = dir.absolutePath,
                isPrimary = volume.isPrimary,
                isRemovable = volume.isRemovable
            )
        }
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
