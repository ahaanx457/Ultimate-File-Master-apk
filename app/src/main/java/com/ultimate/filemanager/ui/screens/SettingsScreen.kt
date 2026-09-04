package com.ultimate.filemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ultimate.filemanager.ui.components.AccentListCard
import com.ultimate.filemanager.ui.components.SectionHeader
import com.ultimate.filemanager.ui.theme.CategoryColors

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {

    var analyticsEnabled by remember {
        mutableStateOf(false)
    }

    var cloudEnabled by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(title = "Privacy & Services")

        Spacer(modifier = Modifier.height(10.dp))

        AccentListCard(
            title = "Analytics",
            description = "Privacy-conscious analytics. Disabled by default.",
            icon = Icons.Outlined.Security,
            accentColor = CategoryColors.Downloads,
            trailing = {
                Switch(
                    checked = analyticsEnabled,
                    onCheckedChange = { analyticsEnabled = it }
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AccentListCard(
            title = "Cloud Storage",
            description = "Cloud provider integration foundation.",
            icon = Icons.Outlined.Cloud,
            accentColor = CategoryColors.Documents,
            trailing = {
                Switch(
                    checked = cloudEnabled,
                    onCheckedChange = { cloudEnabled = it }
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AccentListCard(
            title = "Vault",
            description = "Secure encrypted file storage foundation.",
            icon = Icons.Outlined.Lock,
            accentColor = CategoryColors.Code
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Application")

        Spacer(modifier = Modifier.height(10.dp))

        AccentListCard(
            title = "Ultimate File Manager",
            description = "Version 0.1.0",
            icon = Icons.Outlined.Info,
            accentColor = CategoryColors.Other
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
