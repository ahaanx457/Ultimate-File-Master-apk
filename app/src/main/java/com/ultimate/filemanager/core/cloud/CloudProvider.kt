package com.ultimate.filemanager.core.cloud

interface CloudProvider {

    val id: String

    val displayName: String

    suspend fun authenticate():
        Result<Unit>

    suspend fun list(
        path: String
    ): Result<List<CloudItem>>

    suspend fun upload(
        localPath: String,
        remotePath: String
    ): Result<Unit>

    suspend fun download(
        remotePath: String,
        localPath: String
    ): Result<Unit>

    suspend fun signOut():
        Result<Unit>
}

data class CloudItem(

    val id: String,

    val name: String,

    val isDirectory: Boolean,

    val sizeBytes: Long?
)
