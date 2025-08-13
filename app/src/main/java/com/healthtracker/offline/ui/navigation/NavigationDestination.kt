package com.healthtracker.offline.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation destinations for the app.
 * 
 * Defines all screens and their routing information.
 */
sealed class NavigationDestination(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Bottom Navigation Destinations
    object Home : NavigationDestination(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )
    
    object Doctors : NavigationDestination(
        route = "doctors",
        title = "Doctors",
        icon = Icons.Default.Person
    )
    
    object Institutions : NavigationDestination(
        route = "institutions",
        title = "Institutions",
        icon = Icons.Default.LocationOn
    )
    
    object Search : NavigationDestination(
        route = "search",
        title = "Search",
        icon = Icons.Default.Search
    )
    
    object ExportImport : NavigationDestination(
        route = "export_import",
        title = "Export/Import",
        icon = Icons.Default.ImportExport
    )
    
    object Settings : NavigationDestination(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
    
    // Detail Screens
    object DoctorDetail : NavigationDestination(
        route = "doctor_detail/{doctorId}",
        title = "Doctor Details"
    ) {
        fun createRoute(doctorId: Int) = "doctor_detail/$doctorId"
    }
    
    object DoctorAddEdit : NavigationDestination(
        route = "doctor_add_edit?doctorId={doctorId}",
        title = "Add/Edit Doctor"
    ) {
        fun createRoute(doctorId: Int? = null) = if (doctorId != null) {
            "doctor_add_edit?doctorId=$doctorId"
        } else {
            "doctor_add_edit"
        }
    }
    
    object InstitutionDetail : NavigationDestination(
        route = "institution_detail/{institutionId}",
        title = "Institution Details"
    ) {
        fun createRoute(institutionId: Int) = "institution_detail/$institutionId"
    }
    
    object InstitutionAddEdit : NavigationDestination(
        route = "institution_add_edit?institutionId={institutionId}",
        title = "Add/Edit Institution"
    ) {
        fun createRoute(institutionId: Int? = null) = if (institutionId != null) {
            "institution_add_edit?institutionId=$institutionId"
        } else {
            "institution_add_edit"
        }
    }
    
    object WardAddEdit : NavigationDestination(
        route = "ward_add_edit/{institutionId}?wardId={wardId}",
        title = "Add/Edit Ward"
    ) {
        fun createRoute(institutionId: Int, wardId: Int? = null) = if (wardId != null) {
            "ward_add_edit/$institutionId?wardId=$wardId"
        } else {
            "ward_add_edit/$institutionId"
        }
    }
    
    object AssignmentAddEdit : NavigationDestination(
        route = "assignment_add_edit?doctorId={doctorId}&institutionId={institutionId}&assignmentId={assignmentId}",
        title = "Add/Edit Assignment"
    ) {
        fun createRoute(doctorId: Int? = null, institutionId: Int? = null, assignmentId: Int? = null): String {
            val params = mutableListOf<String>()
            doctorId?.let { params.add("doctorId=$it") }
            institutionId?.let { params.add("institutionId=$it") }
            assignmentId?.let { params.add("assignmentId=$it") }
            
            return if (params.isNotEmpty()) {
                "assignment_add_edit?${params.joinToString("&")}"
            } else {
                "assignment_add_edit"
            }
        }
    }
    
    object SearchResults : NavigationDestination(
        route = "search_results?query={query}",
        title = "Search Results"
    ) {
        fun createRoute(query: String) = "search_results?query=$query"
    }
    
    companion object {
        /**
         * Bottom navigation destinations
         */
        val bottomNavDestinations = listOf(
            Home,
            Doctors,
            Institutions,
            Search,
            ExportImport,
            Settings
        )
        
        /**
         * All navigation destinations
         */
        val allDestinations = listOf(
            Home,
            Doctors,
            Institutions,
            Search,
            ExportImport,
            Settings,
            DoctorDetail,
            DoctorAddEdit,
            InstitutionDetail,
            InstitutionAddEdit,
            WardAddEdit,
            AssignmentAddEdit,
            SearchResults
        )
    }
}