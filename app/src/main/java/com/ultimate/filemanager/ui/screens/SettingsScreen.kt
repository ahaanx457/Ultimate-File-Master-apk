package com.ultimate.filemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp)

    ) {

        Text(
            "Settings",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Text(
            "Privacy & Services",
            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Row(
                modifier =
                    Modifier.padding(16.dp)
            ) {

                Icon(
                    Icons.Outlined.Security,
                    contentDescription = null
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        "Analytics",
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        "Privacy-conscious analytics. Disabled by default.",
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked =
                        analyticsEnabled,

                    onCheckedChange = {
                        analyticsEnabled = it
                    }
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Row(
                modifier =
                    Modifier.padding(16.dp)
            ) {

                Icon(
                    Icons.Outlined.Cloud,
                    contentDescription = null
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        "Cloud Storage",
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        "Cloud provider integration foundation.",
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked =
                        cloudEnabled,

                    onCheckedChange = {
                        cloudEnabled = it
                    }
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Row(
                modifier =
                    Modifier.padding(16.dp)
            ) {

                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Column {

                    Text(
                        "Vault",
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        "Secure encrypted file storage foundation.",
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            "Application",
            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            "Ultimate File Manager",
            style =
                MaterialTheme.typography.bodyLarge
        )

        Text(
            "Version 0.1.0",
            style =
                MaterialTheme.typography.bodySmall
        )
    }
}
