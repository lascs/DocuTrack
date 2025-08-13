package com.healthtracker.offline.ui.navigation

/**
 * Navigation destinations for the Offline Doctor Tracker app.
 * 
 * Defines all screen routes and navigation arguments.
 */
object NavigationDestinations {
    
    // ========== MAIN DESTINATIONS ==========
    
    const val HOME = "home"
    const val DOCTORS = "doctors"
    const val INSTITUTIONS = "institutions"
    const val SEARCH = "search"
    const val EXPORT_IMPORT = "export_import"
    const val SETTINGS = "settings"
    
    // ========== DOCTOR DESTINATIONS ==========
    
    const val DOCTOR_LIST = "doctor_list"
    const val DOCTOR_DETAIL = "doctor_detail"
    const val DOCTOR_ADD = "doctor_add"
    const val DOCTOR_EDIT = "doctor_edit"
    
    // ========== INSTITUTION DESTINATIONS ==========
    
    const val INSTITUTION_LIST = "institution_list"
    const val INSTITUTION_DETAIL = "institution_detail"
    const val INSTITUTION_ADD = "institution_add"
    const val INSTITUTION_EDIT = "institution_edit"
    const val WARD_ADD = "ward_add"
    const val WARD_EDIT = "ward_edit"
    
    // ========== SEARCH DESTINATIONS ==========
    
    const val SEARCH_MAIN = "search_main"
    const val SEARCH_RESULTS = "search_results"
    const val SEARCH_FILTERS = "search_filters"
    const val SAVED_FILTERS = "saved_filters"
    
    // ========== EXPORT/IMPORT DESTINATIONS ==========
    
    const val EXPORT_MAIN = "export_main"
    const val IMPORT_MAIN = "import_main"
    const val BACKUP_RESTORE = "backup_restore"
    
    // ========== SETTINGS DESTINATIONS ==========
    
    const val SETTINGS_MAIN = "settings_main"
    const val SETTINGS_THEME = "settings_theme"
    const val SETTINGS_BACKUP = "settings_backup"
    const val SETTINGS_ABOUT = "settings_about"
    
    // ========== ROUTE BUILDERS ==========
    
    fun doctorDetail(doctorId: Int) = "doctor_detail/$doctorId"
    fun doctorEdit(doctorId: Int) = "doctor_edit/$doctorId"
    
    fun institutionDetail(institutionId: Int) = "institution_detail/$institutionId"
    fun institutionEdit(institutionId: Int) = "institution_edit/$institutionId"
    
    fun wardAdd(institutionId: Int) = "ward_add/$institutionId"
    fun wardEdit(wardId: Int) = "ward_edit/$wardId"
    
    fun searchResults(query: String) = "search_results?query=$query"
    
    // ========== ROUTE PATTERNS ==========
    
    const val DOCTOR_DETAIL_PATTERN = "doctor_detail/{doctorId}"
    const val DOCTOR_EDIT_PATTERN = "doctor_edit/{doctorId}"
    
    const val INSTITUTION_DETAIL_PATTERN = "institution_detail/{institutionId}"
    const val INSTITUTION_EDIT_PATTERN = "institution_edit/{institutionId}"
    
    const val WARD_ADD_PATTERN = "ward_add/{institutionId}"
    const val WARD_EDIT_PATTERN = "ward_edit/{wardId}"
    
    const val SEARCH_RESULTS_PATTERN = "search_results?query={query}"
    
    // ========== ARGUMENT KEYS ==========
    
    const val ARG_DOCTOR_ID = "doctorId"
    const val ARG_INSTITUTION_ID = "institutionId"
    const val ARG_WARD_ID = "wardId"
    const val ARG_SEARCH_QUERY = "query"
}

/**
 * Bottom navigation items
 */
enum class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val iconRes: Int,
    val selectedIconRes: Int? = null
) {
    HOME(
        route = NavigationDestinations.HOME,
        titleRes = com.healthtracker.offline.R.string.nav_home,
        iconRes = androidx.compose.material.icons.Icons.Default.Home.hashCode()
    ),
    DOCTORS(
        route = NavigationDestinations.DOCTORS,
        titleRes = com.healthtracker.offline.R.string.nav_doctors,
        iconRes = androidx.compose.material.icons.Icons.Default.Person.hashCode()
    ),
    INSTITUTIONS(
        route = NavigationDestinations.INSTITUTIONS,
        titleRes = com.healthtracker.offline.R.string.nav_institutions,
        iconRes = androidx.compose.material.icons.Icons.Default.Business.hashCode()
    ),
    SEARCH(
        route = NavigationDestinations.SEARCH,
        titleRes = com.healthtracker.offline.R.string.nav_search,
        iconRes = androidx.compose.material.icons.Icons.Default.Search.hashCode()
    ),
    EXPORT_IMPORT(
        route = NavigationDestinations.EXPORT_IMPORT,
        titleRes = com.healthtracker.offline.R.string.nav_export_import,
        iconRes = androidx.compose.material.icons.Icons.Default.ImportExport.hashCode()
    ),
    SETTINGS(
        route = NavigationDestinations.SETTINGS,
        titleRes = com.healthtracker.offline.R.string.nav_settings,
        iconRes = androidx.compose.material.icons.Icons.Default.Settings.hashCode()
    )
}