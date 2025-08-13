package com.healthtracker.offline.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Placeholder screens for navigation structure.
 * These will be replaced with actual implementations in subsequent tasks.
 */

@Composable
fun HomeScreen(
    onNavigateToAddDoctor: () -> Unit = {},
    onNavigateToAddInstitution: () -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    onNavigateToDoctorDetail: (Int) -> Unit = {},
    onNavigateToInstitutionDetail: (Int) -> Unit = {}
) {
    PlaceholderScreen("Home Screen")
}

@Composable
fun DoctorsScreen(
    onNavigateToAddDoctor: () -> Unit = {},
    onNavigateToDoctorDetail: (Int) -> Unit = {},
    onNavigateToEditDoctor: (Int) -> Unit = {}
) {
    PlaceholderScreen("Doctors Screen")
}

@Composable
fun InstitutionsScreen(
    onNavigateToAddInstitution: () -> Unit = {},
    onNavigateToInstitutionDetail: (Int) -> Unit = {},
    onNavigateToEditInstitution: (Int) -> Unit = {}
) {
    PlaceholderScreen("Institutions Screen")
}

@Composable
fun SearchScreen(
    onNavigateToSearchResults: (String) -> Unit = {},
    onNavigateToDoctorDetail: (Int) -> Unit = {},
    onNavigateToInstitutionDetail: (Int) -> Unit = {}
) {
    PlaceholderScreen("Search Screen")
}

@Composable
fun ExportImportScreen() {
    PlaceholderScreen("Export/Import Screen")
}

@Composable
fun SettingsScreen() {
    PlaceholderScreen("Settings Screen")
}

@Composable
fun DoctorDetailScreen(
    doctorId: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {},
    onNavigateToAddAssignment: () -> Unit = {}
) {
    PlaceholderScreen("Doctor Detail Screen\nDoctor ID: $doctorId")
}

@Composable
fun DoctorAddEditScreen(
    doctorId: Int? = null,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val title = if (doctorId != null) "Edit Doctor\nDoctor ID: $doctorId" else "Add Doctor"
    PlaceholderScreen(title)
}

@Composable
fun InstitutionDetailScreen(
    institutionId: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {},
    onNavigateToAddWard: () -> Unit = {},
    onNavigateToEditWard: (Int) -> Unit = {}
) {
    PlaceholderScreen("Institution Detail Screen\nInstitution ID: $institutionId")
}

@Composable
fun InstitutionAddEditScreen(
    institutionId: Int? = null,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val title = if (institutionId != null) "Edit Institution\nInstitution ID: $institutionId" else "Add Institution"
    PlaceholderScreen(title)
}

@Composable
fun WardAddEditScreen(
    institutionId: Int,
    wardId: Int? = null,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val title = if (wardId != null) {
        "Edit Ward\nInstitution ID: $institutionId\nWard ID: $wardId"
    } else {
        "Add Ward\nInstitution ID: $institutionId"
    }
    PlaceholderScreen(title)
}

@Composable
fun AssignmentAddEditScreen(
    doctorId: Int? = null,
    institutionId: Int? = null,
    assignmentId: Int? = null,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val title = buildString {
        if (assignmentId != null) {
            append("Edit Assignment\nAssignment ID: $assignmentId")
        } else {
            append("Add Assignment")
        }
        doctorId?.let { append("\nDoctor ID: $it") }
        institutionId?.let { append("\nInstitution ID: $it") }
    }
    PlaceholderScreen(title)
}

@Composable
fun SearchResultsScreen(
    initialQuery: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToDoctorDetail: (Int) -> Unit = {},
    onNavigateToInstitutionDetail: (Int) -> Unit = {}
) {
    PlaceholderScreen("Search Results Screen\nQuery: $initialQuery")
}

/**
 * Generic placeholder screen component
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This screen will be implemented in the next task.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}