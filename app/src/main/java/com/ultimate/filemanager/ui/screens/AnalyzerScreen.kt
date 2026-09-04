package com.ultimate.filemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ultimate.filemanager.core.storage.FileOperations
import com.ultimate.filemanager.core.storage.StorageAccessManager
import com.ultimate.filemanager.ui.components.AccentListCard
import com.ultimate.filemanager.ui.components.SectionHeader
import com.ultimate.filemanager.ui.components.StorageOverviewCard
import com.ultimate.filemanager.ui.theme.CategoryColors

@Composable
fun AnalyzerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val storageStats = remember {
        StorageAccessManager(context).getInternalStorageStats()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Storage Analyzer",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "A real-time look at your internal storage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        StorageOverviewCard(
            title = "Internal Storage",
            usedLabel = FileOperations.formatSize(storageStats.usedBytes),
            freeLabel = FileOperations.formatSize(storageStats.freeBytes),
            usedFraction = storageStats.usedFraction
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Coming Soon")

        Spacer(modifier = Modifier.height(10.dp))

        AccentListCard(
            title = "Deep Storage Analysis",
            description = "Category breakdown, largest files and folders, duplicates, and old files — once folder access is scanned.",
            icon = Icons.Outlined.Analytics,
            accentColor = CategoryColors.Folder
        )
    }
}
