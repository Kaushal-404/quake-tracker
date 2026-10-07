package com.example.quakeapplication.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quakeapplication.R
import com.example.quakeapplication.ui.detail.DetailRoute
import com.example.quakeapplication.ui.list.ListRoute
import com.example.quakeapplication.ui.map.MapRoute
import kotlin.reflect.KClass

// The two tabs in the bottom bar
private enum class TopLevelTab(val destination: Any, val routeClass: KClass<*>, val labelRes: Int, val icon: ImageVector) {
    LIST(ListDestination, ListDestination::class, R.string.tab_list, Icons.AutoMirrored.Filled.List),
    MAP(MapDestination, MapDestination::class, R.string.tab_map, Icons.Filled.Place),
}

// The whole app: a bottom bar plus the navigation graph
@Composable
fun QuakeApp() {
    val navController = rememberNavController()
    // Recomposes whenever the user moves to another screen
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // The bottom bar shows only on the two tabs, not on the detail screen
    val showBottomBar = TopLevelTab.entries.any { tab -> currentDestination?.hasRoute(tab.routeClass) == true }

    Scaffold(
        // Each screen handles the status bar itself, so the outer Scaffold adds no padding of its own
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true,
                            onClick = { navController.navigateToTab(tab.destination) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ListDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<ListDestination> {
                ListRoute(onEarthquakeClick = { id -> navController.navigate(DetailDestination(id)) })
            }
            composable<MapDestination> {
                MapRoute(onEarthquakeClick = { id -> navController.navigate(DetailDestination(id)) })
            }
            composable<DetailDestination> {
                DetailRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

// Standard tab switching: keep one copy of each tab, and remember each tab's scroll position
private fun NavHostController.navigateToTab(destination: Any) {
    navigate(destination) {
        // Clear everything above the start screen so the back stack does not grow with every tap
        popUpTo(graph.findStartDestination().id) { saveState = true }
        // Tapping the current tab again does not stack a second copy
        launchSingleTop = true
        // Coming back to a tab restores where the user left it
        restoreState = true
    }
}