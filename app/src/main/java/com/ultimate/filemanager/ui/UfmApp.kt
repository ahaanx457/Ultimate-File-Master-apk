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
import com.ultimate.filemanager.ui.screens.DuplicateFinderScreen
import com.ultimate.filemanager.ui.screens.FileBrowserScreen
import com.ultimate.filemanager.ui.screens.HomeScreen
import com.ultimate.filemanager.ui.screens.SearchScreen
import com.ultimate.filemanager.ui.screens.SettingsScreen
import com.ultimate.filemanager.ui.screens.ToolsScreen
import com.ultimate.filemanager.ui.screens.VaultScreen
import com.ultimate.filemanager.ui.screens.ViewerScreen

private data class BrowserTarget(
    val treeUri: String,
    val title: String
)

private enum class Overlay { SEARCH, DUPLICATE_FINDER, VAULT }

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

    var overlay by remember {
        mutableStateOf<Overlay?>(null)
    }

    BackHandler(enabled = browserTarget != null) {
        browserTarget = null
    }

    BackHandler(enabled = overlay != null) {
        overlay = null
    }

    Scaffold(
        bottomBar = {
            if (browserTarget == null && overlay == null) {
                ModernBottomNavigation(
                    items = navigationItems,
                    selectedIndex = selectedIndex,
                    onSelect = { selectedIndex = it }
                )
            }
        }
    ) { paddingValues ->

        val target = browserTarget
        val currentOverlay = overlay

        when {
            target != null -> {
                FileBrowserScreen(
                    rootTreeUri = target.treeUri,
                    title = target.title,
                    onExit = { browserTarget = null },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            currentOverlay == Overlay.SEARCH -> {
                SearchScreen(
                    onExit = { overlay = null },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            currentOverlay == Overlay.DUPLICATE_FINDER -> {
                DuplicateFinderScreen(
                    onExit = { overlay = null },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            currentOverlay == Overlay.VAULT -> {
                VaultScreen(
                    onExit = { overlay = null },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
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
                            modifier = Modifier.padding(paddingValues),
                            onOpenVault = { overlay = Overlay.VAULT },
                            onOpenSearch = { overlay = Overlay.SEARCH },
                            onOpenDuplicateFinder = { overlay = Overlay.DUPLICATE_FINDER }
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
}
