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
 * Defines all navigation routes and screen compositions.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestinations.HOME,
        modifier = modifier
    ) {
        // ========== HOME ==========
        composable(NavigationDestinations.HOME) {
            HomeScreen(
                onNavigateToAddDoctor = {
                    navController.navigate(NavigationDestinations.DOCTOR_ADD)
                },
                onNavigateToAddInstitution = {
                    navController.navigate(NavigationDestinations.INSTITUTION_ADD)
                },
                onNavigateToSearch = { query ->
                    navController.navigate(NavigationDestinations.searchResults(query))
                },
                onNavigateToDoctors = {
                    navController.navigate(NavigationDestinations.DOCTORS)
                },
                onNavigateToInstitutions = {
                    navController.navigate(NavigationDestinations.INSTITUTIONS)
                }
            )
        }
        
        // ========== DOCTORS ==========
        composable(NavigationDestinations.DOCTORS) {
            DoctorListScreen(
                onNavigateToDetail = { doctorId ->
                    navController.navigate(NavigationDestinations.doctorDetail(doctorId))
                },
                onNavigateToAdd = {
                    navController.navigate(NavigationDestinations.DOCTOR_ADD)
                },
                onNavigateToEdit = { doctorId ->
                    navController.navigate(NavigationDestinations.doctorEdit(doctorId))
                }
            )
        }
        
        composable(
            route = NavigationDestinations.DOCTOR_DETAIL_PATTERN,
            arguments = listOf(
                navArgument(NavigationDestinations.ARG_DOCTOR_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getInt(NavigationDestinations.ARG_DOCTOR_ID) ?: 0
            DoctorDetailScreen(
                doctorId = doctorId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(NavigationDestinations.doctorEdit(doctorId))
                }
            )
        }
        
        composable(NavigationDestinations.DOCTOR_ADD) {
            AddEditDoctorScreen(
                doctorId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = NavigationDestinations.DOCTOR_EDIT_PATTERN,
            arguments = listOf(
                navArgument(NavigationDestinations.ARG_DOCTOR_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getInt(NavigationDestinations.ARG_DOCTOR_ID) ?: 0
            AddEditDoctorScreen(
                doctorId = doctorId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        // ========== INSTITUTIONS ==========
        composable(NavigationDestinations.INSTITUTIONS) {
            InstitutionListScreen(
                onNavigateToDetail = { institutionId ->
                    navController.navigate(NavigationDestinations.institutionDetail(institutionId))
                },
                onNavigateToAdd = {
                    navController.navigate(NavigationDestinations.INSTITUTION_ADD)
                },
                onNavigateToEdit = { institutionId ->
                    navController.navigate(NavigationDestinations.institutionEdit(institutionId))
                }
            )
        }
        
        composable(
            route = NavigationDestinations.INSTITUTION_DETAIL_PATTERN,
            arguments = listOf(
                navArgument(NavigationDestinations.ARG_INSTITUTION_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getInt(NavigationDestinations.ARG_INSTITUTION_ID) ?: 0
            InstitutionDetailScreen(
                institutionId = institutionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(NavigationDestinations.institutionEdit(institutionId))
                },
                onNavigateToAddWard = {
                    navController.navigate(NavigationDestinations.wardAdd(institutionId))
                },
                onNavigateToEditWard = { wardId ->
                    navController.navigate(NavigationDestinations.wardEdit(wardId))
                }
            )
        }
        
        composable(NavigationDestinations.INSTITUTION_ADD) {
            AddEditInstitutionScreen(
                institutionId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = NavigationDestinations.INSTITUTION_EDIT_PATTERN,
            arguments = listOf(
                navArgument(NavigationDestinations.ARG_INSTITUTION_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getInt(NavigationDestinations.ARG_INSTITUTION_ID) ?: 0
            AddEditInstitutionScreen(
                institutionId = institutionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = NavigationDestinations.WARD_ADD_PATTERN,
            arguments = listOf(
                navArgument(NavigationDestinations.ARG_INSTITUTION_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getInt(NavigationDestinations.ARG_INSTITUTION_ID) ?: 0
            AddEditWardScreen(
                wardId = null,
                institutionId = institutionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = NavigationDestinations.WARD_EDIT_PATTERN,
            arguments = listOf(
                navArgument(NavigationDestinations.ARG_WARD_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val wardId = backStackEntry.arguments?.getInt(NavigationDestinations.ARG_WARD_ID) ?: 0
            AddEditWardScreen(
                wardId = wardId,
                institutionId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        // ========== SEARCH ==========
        composable(NavigationDestinations.SEARCH) {
            SearchScreen(
                onNavigateToFilters = {
                    navController.navigate(NavigationDestinations.SEARCH_FILTERS)
                },
                onNavigateToSavedFilters = {
                    navController.navigate(NavigationDestinations.SAVED_FILTERS)
                },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(NavigationDestinations.doctorDetail(doctorId))
                },
                onNavigateToInstitutionDetail = { institutionId ->
                    navController.navigate(NavigationDestinations.institutionDetail(institutionId))
                }
            )
        }
        
        composable(
            route = NavigationDestinations.SEARCH_RESULTS_PATTERN,
            arguments = listOf(
                navArgument(NavigationDestinations.ARG_SEARCH_QUERY) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString(NavigationDestinations.ARG_SEARCH_QUERY) ?: ""
            SearchResultsScreen(
                initialQuery = query,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(NavigationDestinations.doctorDetail(doctorId))
                },
                onNavigateToInstitutionDetail = { institutionId ->
                    navController.navigate(NavigationDestinations.institutionDetail(institutionId))
                }
            )
        }
        
        composable(NavigationDestinations.SEARCH_FILTERS) {
            SearchFiltersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onApplyFilters = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavigationDestinations.SAVED_FILTERS) {
            SavedFiltersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onApplyFilter = {
                    navController.popBackStack()
                }
            )
        }
        
        // ========== EXPORT/IMPORT ==========
        composable(NavigationDestinations.EXPORT_IMPORT) {
            ExportImportScreen(
                onNavigateToExport = {
                    navController.navigate(NavigationDestinations.EXPORT_MAIN)
                },
                onNavigateToImport = {
                    navController.navigate(NavigationDestinations.IMPORT_MAIN)
                },
                onNavigateToBackup = {
                    navController.navigate(NavigationDestinations.BACKUP_RESTORE)
                }
            )
        }
        
        composable(NavigationDestinations.EXPORT_MAIN) {
            ExportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavigationDestinations.IMPORT_MAIN) {
            ImportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavigationDestinations.BACKUP_RESTORE) {
            BackupRestoreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // ========== SETTINGS ==========
        composable(NavigationDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateToTheme = {
                    navController.navigate(NavigationDestinations.SETTINGS_THEME)
                },
                onNavigateToBackup = {
                    navController.navigate(NavigationDestinations.SETTINGS_BACKUP)
                },
                onNavigateToAbout = {
                    navController.navigate(NavigationDestinations.SETTINGS_ABOUT)
                }
            )
        }
        
        composable(NavigationDestinations.SETTINGS_THEME) {
            ThemeSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavigationDestinations.SETTINGS_BACKUP) {
            BackupSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavigationDestinations.SETTINGS_ABOUT) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}