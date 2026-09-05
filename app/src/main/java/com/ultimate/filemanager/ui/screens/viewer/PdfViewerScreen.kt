package com.ultimate.filemanager.ui.screens.viewer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PdfViewerScreen(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pages by remember(uri) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember(uri) { mutableStateOf(true) }
    var failed by remember(uri) { mutableStateOf(false) }

    LaunchedEffect(uri) {
        isLoading = true

        val rendered = withContext(Dispatchers.IO) {
            runCatching {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                    ?: return@runCatching null

                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        (0 until renderer.pageCount).map { index ->
                            renderer.openPage(index).use { page ->
                                val bitmap = Bitmap.createBitmap(
                                    page.width * 2,
                                    page.height * 2,
                                    Bitmap.Config.ARGB_8888
                                )
                                page.render(
                                    bitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                bitmap
                            }
                        }
                    }
                }
            }.getOrNull()
        }

        if (rendered == null) {
            failed = true
        } else {
            pages = rendered
        }
        isLoading = false
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()

            failed || pages.isEmpty() -> Text(
                "Couldn't open this PDF",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pages) { page ->
                        Image(
                            bitmap = page.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
