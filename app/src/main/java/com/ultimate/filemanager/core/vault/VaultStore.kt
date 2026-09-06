package com.ultimate.filemanager.core.vault

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class VaultItem(
    val id: String,
    val originalName: String,
    val mimeType: String,
    val size: Long,
    val dateAdded: Long
)

/**
 * Vault contents live in this app's private storage (filesDir), which the
 * Android sandbox already restricts to this app's own UID — no other app
 * can read it, with or without "All files access". File bytes are also
 * AES/GCM-encrypted on top of that via VaultCrypto, using a key that lives
 * in the Android Keystore and never leaves secure hardware.
 */
object VaultStore {

    private fun vaultDir(context: Context): File =
        File(context.filesDir, "vault").apply { mkdirs() }

    private fun indexFile(context: Context): File =
        File(vaultDir(context), "index.dat")

    private fun blobFile(context: Context, id: String): File =
        File(vaultDir(context), "$id.enc")

    fun listItems(context: Context): List<VaultItem> {
        val file = indexFile(context)
        if (!file.exists()) return emptyList()

        val json = runCatching {
            String(VaultCrypto.decrypt(file.readBytes()), Charsets.UTF_8)
        }.getOrNull() ?: return emptyList()

        val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()

        return (0 until array.length()).mapNotNull { i ->
            runCatching {
                val obj = array.getJSONObject(i)
                VaultItem(
                    id = obj.getString("id"),
                    originalName = obj.getString("originalName"),
                    mimeType = obj.optString("mimeType", "application/octet-stream"),
                    size = obj.getLong("size"),
                    dateAdded = obj.getLong("dateAdded")
                )
            }.getOrNull()
        }
    }

    private fun saveItems(context: Context, items: List<VaultItem>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("originalName", item.originalName)
            obj.put("mimeType", item.mimeType)
            obj.put("size", item.size)
            obj.put("dateAdded", item.dateAdded)
            array.put(obj)
        }
        val encrypted = VaultCrypto.encrypt(array.toString().toByteArray(Charsets.UTF_8))
        indexFile(context).writeBytes(encrypted)
    }

    fun addItem(
        context: Context,
        originalName: String,
        mimeType: String?,
        bytes: ByteArray
    ): VaultItem {
        val id = UUID.randomUUID().toString()
        val encrypted = VaultCrypto.encrypt(bytes)
        blobFile(context, id).writeBytes(encrypted)

        val item = VaultItem(
            id = id,
            originalName = originalName,
            mimeType = mimeType ?: "application/octet-stream",
            size = bytes.size.toLong(),
            dateAdded = System.currentTimeMillis()
        )

        saveItems(context, listItems(context) + item)
        return item
    }

    fun readItemBytes(context: Context, id: String): ByteArray? {
        val file = blobFile(context, id)
        if (!file.exists()) return null
        return runCatching { VaultCrypto.decrypt(file.readBytes()) }.getOrNull()
    }

    fun deleteItem(context: Context, id: String) {
        blobFile(context, id).delete()
        saveItems(context, listItems(context).filterNot { it.id == id })
    }
}
