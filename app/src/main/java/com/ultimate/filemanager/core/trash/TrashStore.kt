package com.ultimate.filemanager.core.trash

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class TrashItem(
    val id: String,
    val originalName: String,
    val originalParentUri: String,
    val mimeType: String,
    val size: Long,
    val dateTrashed: Long
)

/**
 * "Deleting" a file moves its bytes into this app-private trash folder
 * instead of destroying them immediately. Nothing is permanently lost
 * until the user explicitly empties the trash or deletes an item forever.
 */
object TrashStore {

    private fun trashDir(context: Context): File =
        File(context.filesDir, "trash").apply { mkdirs() }

    private fun indexFile(context: Context): File =
        File(trashDir(context), "index.json")

    private fun blobFile(context: Context, id: String): File =
        File(trashDir(context), id)

    fun listItems(context: Context): List<TrashItem> {
        val file = indexFile(context)
        if (!file.exists()) return emptyList()

        val json = runCatching { file.readText() }.getOrNull() ?: return emptyList()
        val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()

        return (0 until array.length()).mapNotNull { i ->
            runCatching {
                val obj = array.getJSONObject(i)
                TrashItem(
                    id = obj.getString("id"),
                    originalName = obj.getString("originalName"),
                    originalParentUri = obj.getString("originalParentUri"),
                    mimeType = obj.optString("mimeType", "application/octet-stream"),
                    size = obj.getLong("size"),
                    dateTrashed = obj.getLong("dateTrashed")
                )
            }.getOrNull()
        }
    }

    private fun saveItems(context: Context, items: List<TrashItem>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("originalName", item.originalName)
            obj.put("originalParentUri", item.originalParentUri)
            obj.put("mimeType", item.mimeType)
            obj.put("size", item.size)
            obj.put("dateTrashed", item.dateTrashed)
            array.put(obj)
        }
        indexFile(context).writeText(array.toString())
    }

    /** Moves [source] (a child of [sourceParent]) into the trash. */
    fun moveToTrash(context: Context, source: DocumentFile, sourceParent: DocumentFile): Boolean {
        val name = source.name ?: return false
        val id = UUID.randomUUID().toString()

        return runCatching {
            val copied = context.contentResolver.openInputStream(source.uri)?.use { input ->
                blobFile(context, id).outputStream().use { output -> input.copyTo(output) }
                true
            } ?: false

            if (!copied) return@runCatching false

            val item = TrashItem(
                id = id,
                originalName = name,
                originalParentUri = sourceParent.uri.toString(),
                mimeType = source.type ?: "application/octet-stream",
                size = source.length(),
                dateTrashed = System.currentTimeMillis()
            )
            saveItems(context, listItems(context) + item)
            source.delete()
            true
        }.getOrDefault(false)
    }

    fun restore(context: Context, item: TrashItem): Boolean {
        return runCatching {
            val parentUri = Uri.parse(item.originalParentUri)

            val parent = if (parentUri.scheme == "file") {
                DocumentFile.fromFile(File(parentUri.path ?: return@runCatching false))
            } else {
                DocumentFile.fromTreeUri(context, parentUri)
            } ?: return@runCatching false

            var targetName = item.originalName
            if (parent.findFile(targetName) != null) {
                val dot = targetName.lastIndexOf('.')
                targetName = if (dot > 0) {
                    "${targetName.substring(0, dot)}_restored${targetName.substring(dot)}"
                } else {
                    "${targetName}_restored"
                }
            }

            val newFile = parent.createFile(item.mimeType, targetName) ?: return@runCatching false

            blobFile(context, item.id).inputStream().use { input ->
                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                    input.copyTo(output)
                }
            }

            blobFile(context, item.id).delete()
            saveItems(context, listItems(context).filterNot { it.id == item.id })
            true
        }.getOrDefault(false)
    }

    fun deleteForever(context: Context, item: TrashItem) {
        blobFile(context, item.id).delete()
        saveItems(context, listItems(context).filterNot { it.id == item.id })
    }

    fun emptyTrash(context: Context) {
        listItems(context).forEach { blobFile(context, it.id).delete() }
        saveItems(context, emptyList())
    }

    fun totalSize(context: Context): Long = listItems(context).sumOf { it.size }
}
