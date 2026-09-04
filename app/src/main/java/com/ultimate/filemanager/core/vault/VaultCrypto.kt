package com.ultimate.filemanager.core.vault

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object VaultCrypto {

    private const val KEYSTORE =
        "AndroidKeyStore"

    private const val KEY_ALIAS =
        "UltimateFileManagerVaultKey"

    private const val TRANSFORMATION =
        "AES/GCM/NoPadding"

    private fun getKey(): SecretKey {

        val keyStore =
            KeyStore
                .getInstance(KEYSTORE)
                .apply {
                    load(null)
                }

        val existing =
            keyStore.getKey(
                KEY_ALIAS,
                null
            ) as? SecretKey

        if (existing != null) {
            return existing
        }

        val generator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE
            )

        val specification =
            KeyGenParameterSpec.Builder(

                KEY_ALIAS,

                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT

            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setUserAuthenticationRequired(false)
                .build()

        generator.init(specification)

        return generator.generateKey()
    }

    fun encrypt(
        data: ByteArray
    ): ByteArray {

        val cipher =
            Cipher.getInstance(
                TRANSFORMATION
            )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getKey()
        )

        val encrypted =
            cipher.doFinal(data)

        /*
         * Payload format:
         *
         * 12 bytes IV
         * remaining bytes ciphertext
         */

        return cipher.iv + encrypted
    }

    fun decrypt(
        payload: ByteArray
    ): ByteArray {

        require(
            payload.size > 12
        ) {
            "Invalid vault payload"
        }

        val iv =
            payload.copyOfRange(
                0,
                12
            )

        val encrypted =
            payload.copyOfRange(
                12,
                payload.size
            )

        val cipher =
            Cipher.getInstance(
                TRANSFORMATION
            )

        cipher.init(

            Cipher.DECRYPT_MODE,

            getKey(),

            GCMParameterSpec(
                128,
                iv
            )
        )

        return cipher.doFinal(
            encrypted
        )
    }
}
