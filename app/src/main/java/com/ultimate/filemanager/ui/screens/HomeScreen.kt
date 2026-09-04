package com.ultimate.filemanager.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class FileCategory(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {

    var accessGranted by remember {
        mutableStateOf(false)
    }

    val folderPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            accessGranted = uri != null
        }

    val categories = listOf(
        FileCategory("Documents", Icons.Outlined.Description),
        FileCategory("Images", Icons.Outlined.Image),
        FileCategory("Videos", Icons.Outlined.Movie),
        FileCategory("Music", Icons.Outlined.MusicNote),
        FileCategory("Archives", Icons.Outlined.Inventory2),
        FileCategory("Downloads", Icons.Outlined.Download),
        FileCategory("Code", Icons.Outlined.Code),
        FileCategory("Other", Icons.Outlined.InsertDriveFile)
    )

    // Grouped in pairs so the grid can be drawn with plain Rows instead of
    // a nested LazyVerticalGrid. Nesting one scrollable/lazy component
    // inside another (or inside a non-scrollable Column that also has to
    // size a fillMaxSize() lazy child) is a common source of Compose
    // measurement crashes on first launch. A single top-level LazyColumn
    // with plain Rows as items avoids that entirely.
    val categoryRows = categories.chunked(2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        item {
            Text(
                text = "Ultimate File Manager",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Manage your files from one place.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {

                    Text(
                        "Internal Storage",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Storage scanner foundation",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Storage analysis will be connected to the real scanner.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { folderPicker.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (accessGranted) "Folder Access Granted"
                    else "Grant Folder Access"
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "Quick Access",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(categoryRows) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { category ->
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Icon(category.icon, contentDescription = category.name)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(category.name)
                        }
                    }
                }
                // Pad the row with an empty weighted spacer if the last
                // row has an odd number of items, so cards stay aligned.
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
