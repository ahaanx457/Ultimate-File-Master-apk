package com.ultimate.filemanager

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.MediaStore
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Catches any uncaught crash and, instead of letting the app silently die,
 * launches an on-screen crash report (CrashActivity) so the stack trace is
 * visible immediately — no ADB, no computer, no file browsing required.
 *
 * It also attempts to save a copy to the public Downloads folder as a
 * secondary channel, but the on-screen route is the reliable one.
 */
class UfmApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val stringWriter = StringWriter()
            throwable.printStackTrace(PrintWriter(stringWriter))
            val trace = stringWriter.toString()

            try {
                saveCrashLog(this, trace)
            } catch (ignored: Throwable) {
                // Never let logging itself block the crash screen.
            }

            try {
                val intent = Intent(this, CrashActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                    putExtra(CrashActivity.EXTRA_TRACE, trace)
                }
                startActivity(intent)
            } catch (ignored: Throwable) {
                // If even this fails, fall through to process death below.
            }

            Process.killProcess(Process.myPid())
            System.exit(10)
        }
    }

    private fun saveCrashLog(context: Context, stackTrace: String) {
        val timestamp =
            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val fileName = "ufm_crash_$timestamp.txt"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
            }

            val resolver = context.contentResolver
            val uri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(stackTrace.toByteArray())
                }
            }
        } else {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
            File(downloadsDir, fileName).writeText(stackTrace)
        }
    }
}
