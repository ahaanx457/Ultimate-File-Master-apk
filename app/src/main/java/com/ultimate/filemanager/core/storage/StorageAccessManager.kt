package com.ultimate.filemanager.core.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile

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
