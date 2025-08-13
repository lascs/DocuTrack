package com.healthtracker.offline.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.healthtracker.offline.ui.screens.*

/**
 * Main navigation component for the app.
 * 
 * Defines the navigation graph and screen routing.
 */
@Composable
fun OfflineDoctorTrackerNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Home.route,
        modifier = modifier
    ) {
        // Home Screen
        composable(NavigationDestination.Home.route) {
            HomeScreen(
                onNavigateToAddDoctor = {
                    navController.navigate(NavigationDestination.DoctorAddEdit.createRoute())
                },
                onNavigateToAddInstitution = {
                    navController.navigate(NavigationDestination.InstitutionAddEdit.createRoute())
                },
                onNavigateToSearch = { query ->
                    navController.navigate(NavigationDestination.SearchResults.createRoute(query))
                },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(NavigationDestination.DoctorDetail.createRoute(doctorId))
                },
                onNavigateToInstitutionDetail = { institutionId ->
                    navController.navigate(NavigationDestination.InstitutionDetail.createRoute(institutionId))
                }
            )
        }
        
        // Doctors Screen
        composable(NavigationDestination.Doctors.route) {
            DoctorsScreen(
                onNavigateToAddDoctor = {
                    navController.navigate(NavigationDestination.DoctorAddEdit.createRoute())
                },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(NavigationDestination.DoctorDetail.createRoute(doctorId))
                },
                onNavigateToEditDoctor = { doctorId ->
                    navController.navigate(NavigationDestination.DoctorAddEdit.createRoute(doctorId))
                }
            )
        }
        
        // Institutions Screen
        composable(NavigationDestination.Institutions.route) {
            InstitutionsScreen(
                onNavigateToAddInstitution = {
                    navController.navigate(NavigationDestination.InstitutionAddEdit.createRoute())
                },
                onNavigateToInstitutionDetail = { institutionId ->
                    navController.navigate(NavigationDestination.InstitutionDetail.createRoute(institutionId))
                },
                onNavigateToEditInstitution = { institutionId ->
                    navController.navigate(NavigationDestination.InstitutionAddEdit.createRoute(institutionId))
                }
            )
        }
        
        // Search Screen
        composable(NavigationDestination.Search.route) {
            SearchScreen(
                onNavigateToSearchResults = { query ->
                    navController.navigate(NavigationDestination.SearchResults.createRoute(query))
                },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(NavigationDestination.DoctorDetail.createRoute(doctorId))
                },
                onNavigateToInstitutionDetail = { institutionId ->
                    navController.navigate(NavigationDestination.InstitutionDetail.createRoute(institutionId))
                }
            )
        }
        
        // Export/Import Screen
        composable(NavigationDestination.ExportImport.route) {
            ExportImportScreen()
        }
        
        // Settings Screen
        composable(NavigationDestination.Settings.route) {
            SettingsScreen()
        }
        
        // Doctor Detail Screen
        composable(
            route = NavigationDestination.DoctorDetail.route,
            arguments = listOf(
                navArgument("doctorId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: return@composable
            DoctorDetailScreen(
                doctorId = doctorId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { 
                    navController.navigate(NavigationDestination.DoctorAddEdit.createRoute(doctorId))
                },
                onNavigateToAddAssignment = {
                    navController.navigate(NavigationDestination.AssignmentAddEdit.createRoute(doctorId = doctorId))
                }
            )
        }
        
        // Doctor Add/Edit Screen
        composable(
            route = NavigationDestination.DoctorAddEdit.route,
            arguments = listOf(
                navArgument("doctorId") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getInt("doctorId")?.takeIf { it != -1 }
            DoctorAddEditScreen(
                doctorId = doctorId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Institution Detail Screen
        composable(
            route = NavigationDestination.InstitutionDetail.route,
            arguments = listOf(
                navArgument("institutionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getInt("institutionId") ?: return@composable
            InstitutionDetailScreen(
                institutionId = institutionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(NavigationDestination.InstitutionAddEdit.createRoute(institutionId))
                },
                onNavigateToAddWard = {
                    navController.navigate(NavigationDestination.WardAddEdit.createRoute(institutionId))
                },
                onNavigateToEditWard = { wardId ->
                    navController.navigate(NavigationDestination.WardAddEdit.createRoute(institutionId, wardId))
                }
            )
        }
        
        // Institution Add/Edit Screen
        composable(
            route = NavigationDestination.InstitutionAddEdit.route,
            arguments = listOf(
                navArgument("institutionId") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getInt("institutionId")?.takeIf { it != -1 }
            InstitutionAddEditScreen(
                institutionId = institutionId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Ward Add/Edit Screen
        composable(
            route = NavigationDestination.WardAddEdit.route,
            arguments = listOf(
                navArgument("institutionId") { type = NavType.IntType },
                navArgument("wardId") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getInt("institutionId") ?: return@composable
            val wardId = backStackEntry.arguments?.getInt("wardId")?.takeIf { it != -1 }
            WardAddEditScreen(
                institutionId = institutionId,
                wardId = wardId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Assignment Add/Edit Screen
        composable(
            route = NavigationDestination.AssignmentAddEdit.route,
            arguments = listOf(
                navArgument("doctorId") { 
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("institutionId") { 
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("assignmentId") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getInt("doctorId")?.takeIf { it != -1 }
            val institutionId = backStackEntry.arguments?.getInt("institutionId")?.takeIf { it != -1 }
            val assignmentId = backStackEntry.arguments?.getInt("assignmentId")?.takeIf { it != -1 }
            
            AssignmentAddEditScreen(
                doctorId = doctorId,
                institutionId = institutionId,
                assignmentId = assignmentId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // Search Results Screen
        composable(
            route = NavigationDestination.SearchResults.route,
            arguments = listOf(
                navArgument("query") { 
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultsScreen(
                initialQuery = query,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(NavigationDestination.DoctorDetail.createRoute(doctorId))
                },
                onNavigateToInstitutionDetail = { institutionId ->
                    navController.navigate(NavigationDestination.InstitutionDetail.createRoute(institutionId))
                }
            )
        }
    }
}

/**
 * Navigation actions for use in screens
 */
data class NavigationActions(
    val navigateUp: () -> Unit,
    val navigateToHome: () -> Unit,
    val navigateToDoctors: () -> Unit,
    val navigateToInstitutions: () -> Unit,
    val navigateToSearch: () -> Unit,
    val navigateToExportImport: () -> Unit,
    val navigateToSettings: () -> Unit,
    val navigateToDoctorDetail: (Int) -> Unit,
    val navigateToInstitutionDetail: (Int) -> Unit,
    val navigateToAddDoctor: () -> Unit,
    val navigateToEditDoctor: (Int) -> Unit,
    val navigateToAddInstitution: () -> Unit,
    val navigateToEditInstitution: (Int) -> Unit,
    val navigateToAddWard: (Int) -> Unit,
    val navigateToEditWard: (Int, Int) -> Unit,
    val navigateToAddAssignment: (Int?, Int?) -> Unit,
    val navigateToSearchResults: (String) -> Unit
)

/**
 * Creates navigation actions from NavController
 */
fun createNavigationActions(navController: NavHostController): NavigationActions {
    return NavigationActions(
        navigateUp = { navController.navigateUp() },
        navigateToHome = { navController.navigate(NavigationDestination.Home.route) },
        navigateToDoctors = { navController.navigate(NavigationDestination.Doctors.route) },
        navigateToInstitutions = { navController.navigate(NavigationDestination.Institutions.route) },
        navigateToSearch = { navController.navigate(NavigationDestination.Search.route) },
        navigateToExportImport = { navController.navigate(NavigationDestination.ExportImport.route) },
        navigateToSettings = { navController.navigate(NavigationDestination.Settings.route) },
        navigateToDoctorDetail = { doctorId ->
            navController.navigate(NavigationDestination.DoctorDetail.createRoute(doctorId))
        },
        navigateToInstitutionDetail = { institutionId ->
            navController.navigate(NavigationDestination.InstitutionDetail.createRoute(institutionId))
        },
        navigateToAddDoctor = {
            navController.navigate(NavigationDestination.DoctorAddEdit.createRoute())
        },
        navigateToEditDoctor = { doctorId ->
            navController.navigate(NavigationDestination.DoctorAddEdit.createRoute(doctorId))
        },
        navigateToAddInstitution = {
            navController.navigate(NavigationDestination.InstitutionAddEdit.createRoute())
        },
        navigateToEditInstitution = { institutionId ->
            navController.navigate(NavigationDestination.InstitutionAddEdit.createRoute(institutionId))
        },
        navigateToAddWard = { institutionId ->
            navController.navigate(NavigationDestination.WardAddEdit.createRoute(institutionId))
        },
        navigateToEditWard = { institutionId, wardId ->
            navController.navigate(NavigationDestination.WardAddEdit.createRoute(institutionId, wardId))
        },
        navigateToAddAssignment = { doctorId, institutionId ->
            navController.navigate(NavigationDestination.AssignmentAddEdit.createRoute(doctorId, institutionId))
        },
        navigateToSearchResults = { query ->
            navController.navigate(NavigationDestination.SearchResults.createRoute(query))
        }
    )
}