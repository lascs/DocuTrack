package com.healthtracker.offline.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.healthtracker.offline.ui.navigation.NavigationDestination
import com.healthtracker.offline.R

/**
 * Bottom navigation bar component.
 * 
 * Provides navigation between main app sections.
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        modifier = modifier
    ) {
        NavigationDestination.bottomNavDestinations.forEach { destination ->
            NavigationBarItem(
                icon = {
                    destination.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = destination.title
                        )
                    }
                },
                label = {
                    Text(text = destination.title)
                },
                selected = currentRoute == destination.route,
                onClick = {
                    if (currentRoute != destination.route) {
                        navController.navigate(destination.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Determines if the bottom navigation should be shown for the current route
 */
fun shouldShowBottomNavigation(currentRoute: String?): Boolean {
    return NavigationDestination.bottomNavDestinations.any { it.route == currentRoute }
}