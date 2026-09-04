package com.ultimate.filemanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ultimate.filemanager.ui.components.ModernBottomNavigation
import com.ultimate.filemanager.ui.components.NavItem
import com.ultimate.filemanager.ui.screens.AnalyzerScreen
import com.ultimate.filemanager.ui.screens.FileBrowserScreen
import com.ultimate.filemanager.ui.screens.HomeScreen
import com.ultimate.filemanager.ui.screens.SettingsScreen
import com.ultimate.filemanager.ui.screens.ToolsScreen
import com.ultimate.filemanager.ui.screens.ViewerScreen

private data class BrowserTarget(
    val treeUri: String,
    val title: String
)

@Composable
fun UfmApp() {

    val navigationItems = listOf(
        NavItem("Home", Icons.Outlined.Folder),
        NavItem("Tools", Icons.Outlined.Build),
        NavItem("Analyzer", Icons.Outlined.Analytics),
        NavItem("Viewer", Icons.Outlined.Visibility),
        NavItem("Settings", Icons.Outlined.Settings)
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    var browserTarget by remember {
        mutableStateOf<BrowserTarget?>(null)
    }

    BackHandler(enabled = browserTarget != null) {
        browserTarget = null
    }

    Scaffold(
        bottomBar = {
            if (browserTarget == null) {
                ModernBottomNavigation(
                    items = navigationItems,
                    selectedIndex = selectedIndex,
                    onSelect = { selectedIndex = it }
                )
            }
        }
    ) { paddingValues ->

        val target = browserTarget

        if (target != null) {
            FileBrowserScreen(
                rootTreeUri = target.treeUri,
                title = target.title,
                onExit = { browserTarget = null },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            when (selectedIndex) {

                0 ->
                    HomeScreen(
                        modifier = Modifier.padding(paddingValues),
                        onBrowse = { uri, title ->
                            browserTarget = BrowserTarget(uri, title)
                        }
                    )

                1 ->
                    ToolsScreen(
                        modifier = Modifier.padding(paddingValues)
                    )

                2 ->
                    AnalyzerScreen(
                        modifier = Modifier.padding(paddingValues)
                    )

                3 ->
                    ViewerScreen(
                        modifier = Modifier.padding(paddingValues)
                    )

                4 ->
                    SettingsScreen(
                        modifier = Modifier.padding(paddingValues)
                    )
            }
        }
    }
}
