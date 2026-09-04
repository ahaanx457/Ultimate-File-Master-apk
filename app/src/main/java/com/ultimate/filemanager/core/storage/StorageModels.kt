package com.ultimate.filemanager.core.storage

data class StorageLocation(

    val id: String,

    val displayName: String,

    val path: String?,

    val type: StorageType,

    val isRemovable: Boolean
)

enum class StorageType {

    INTERNAL,

    REMOVABLE,

    OTG,

    SAF_TREE,

    ROOT
}
