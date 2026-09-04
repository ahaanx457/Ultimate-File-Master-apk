package com.ultimate.filemanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ultimate.filemanager.ui.screens.AnalyzerScreen
import com.ultimate.filemanager.ui.screens.HomeScreen
import com.ultimate.filemanager.ui.screens.SettingsScreen
import com.ultimate.filemanager.ui.screens.ToolsScreen
import com.ultimate.filemanager.ui.screens.ViewerScreen

private data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun UfmApp() {

    val navigationItems = listOf(

        NavigationItem(
            "Home",
            Icons.Outlined.Folder
        ),

        NavigationItem(
            "Tools",
            Icons.Outlined.Build
        ),

        NavigationItem(
            "Analyzer",
            Icons.Outlined.Analytics
        ),

        NavigationItem(
            "Viewer",
            Icons.Outlined.Visibility
        ),

        NavigationItem(
            "Settings",
            Icons.Outlined.Settings
        )
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    Scaffold(

        bottomBar = {

            NavigationBar {

                navigationItems.forEachIndexed {
                        index,
                        item ->

                    NavigationBarItem(

                        selected =
                            selectedIndex == index,

                        onClick = {
                            selectedIndex = index
                        },

                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription =
                                    item.title
                            )
                        },

                        label = {
                            Text(item.title)
                        }
                    )
                }
            }
        }

    ) { paddingValues ->

        when (selectedIndex) {

            0 ->
                HomeScreen(
                    modifier =
                        Modifier.padding(paddingValues)
                )

            1 ->
                ToolsScreen(
                    modifier =
                        Modifier.padding(paddingValues)
                )

            2 ->
                AnalyzerScreen(
                    modifier =
                        Modifier.padding(paddingValues)
                )

            3 ->
                ViewerScreen(
                    modifier =
                        Modifier.padding(paddingValues)
                )

            4 ->
                SettingsScreen(
                    modifier =
                        Modifier.padding(paddingValues)
                )
        }
    }
}
