package com.healthtracker.offline.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthtracker.offline.ui.viewmodels.HomeViewModel
import com.healthtracker.offline.ui.components.SearchBar
import com.healthtracker.offline.ui.components.StatCard
import com.healthtracker.offline.ui.components.QuickActionCard
import com.healthtracker.offline.ui.components.RecentItemCard
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.entities.Institution

/**
 * Home screen with dashboard overview and quick actions.
 * 
 * Displays statistics, recent updates, and provides quick access to main features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddDoctor: () -> Unit,
    onNavigateToAddInstitution: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToDoctorDetail: (Int) -> Unit,
    onNavigateToInstitutionDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val recentDoctors by viewModel.recentDoctors.collectAsStateWithLifecycle()
    val recentInstitutions by viewModel.recentInstitutions.collectAsStateWithLifecycle()
    val topSpecialities by viewModel.topSpecialities.collectAsStateWithLifecycle()
    val topAreaBricks by viewModel.topAreaBricks.collectAsStateWithLifecycle()
    val quickSearchQuery by viewModel.quickSearchQuery.collectAsStateWithLifecycle()
    val quickSearchResults by viewModel.quickSearchResults.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Section
        item {
            WelcomeSection()
        }
        
        // Quick Search
        item {
            QuickSearchSection(
                query = quickSearchQuery,
                onQueryChange = viewModel::updateQuickSearchQuery,
                searchResults = quickSearchResults,
                onSearch = onNavigateToSearch,
                onDoctorClick = onNavigateToDoctorDetail,
                onInstitutionClick = onNavigateToInstitutionDetail,
                onClearSearch = viewModel::clearQuickSearch
            )
        }
        
        // Quick Actions
        item {
            QuickActionsSection(
                onAddDoctor = onNavigateToAddDoctor,
                onAddInstitution = onNavigateToAddInstitution,
                onSearch = { onNavigateToSearch("") }
            )
        }
        
        // Dashboard Statistics
        item {
            DashboardStatsSection(
                stats = dashboardStats,
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refreshDashboard
            )
        }
        
        // Recent Doctors
        if (recentDoctors.isNotEmpty()) {
            item {
                RecentDoctorsSection(
                    doctors = recentDoctors,
                    onDoctorClick = onNavigateToDoctorDetail
                )
            }
        }
        
        // Recent Institutions
        if (recentInstitutions.isNotEmpty()) {
            item {
                RecentInstitutionsSection(
                    institutions = recentInstitutions,
                    onInstitutionClick = onNavigateToInstitutionDetail
                )
            }
        }
        
        // Top Specialities
        if (topSpecialities.isNotEmpty()) {
            item {
                TopSpecialitiesSection(
                    specialities = topSpecialities
                )
            }
        }
        
        // Top Area Bricks
        if (topAreaBricks.isNotEmpty()) {
            item {
                TopAreaBricksSection(
                    areaBricks = topAreaBricks
                )
            }
        }
    }
    
    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or dialog
            viewModel.clearError()
        }
    }
}

@Composable
private fun WelcomeSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Offline Doctor Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manage doctors and institutions completely offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun QuickSearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: com.healthtracker.offline.ui.viewmodels.QuickSearchResults,
    onSearch: (String) -> Unit,
    onDoctorClick: (Int) -> Unit,
    onInstitutionClick: (Int) -> Unit,
    onClearSearch: () -> Unit
) {
    Column {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            placeholder = "Quick search doctors or institutions...",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Quick search results
        if (query.isNotBlank() && (searchResults.doctors.isNotEmpty() || searchResults.institutions.isNotEmpty())) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (searchResults.doctors.isNotEmpty()) {
                        Text(
                            text = "Doctors",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        searchResults.doctors.forEach { doctor ->
                            QuickSearchResultItem(
                                title = doctor.name,
                                subtitle = doctor.speciality,
                                onClick = { onDoctorClick(doctor.doctorId) }
                            )
                        }
                    }
                    
                    if (searchResults.institutions.isNotEmpty()) {
                        if (searchResults.doctors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Text(
                            text = "Institutions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        searchResults.institutions.forEach { institution ->
                            QuickSearchResultItem(
                                title = institution.name,
                                subtitle = institution.areaBrick,
                                onClick = { onInstitutionClick(institution.institutionId) }
                            )
                        }
                    }
                    
                    if (searchResults.hasMoreResults) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { onSearch(query) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View all results")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSearchResultItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAddDoctor: () -> Unit,
    onAddInstitution: () -> Unit,
    onSearch: () -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionCard(
                    title = "Add Doctor",
                    icon = Icons.Default.PersonAdd,
                    onClick = onAddDoctor
                )
            }
            item {
                QuickActionCard(
                    title = "Add Institution",
                    icon = Icons.Default.Add,
                    onClick = onAddInstitution
                )
            }
            item {
                QuickActionCard(
                    title = "Advanced Search",
                    icon = Icons.Default.Search,
                    onClick = onSearch
                )
            }
        }
    }
}

@Composable
private fun DashboardStatsSection(
    stats: com.healthtracker.offline.ui.viewmodels.DashboardStats,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatCard(
                    title = "Doctors",
                    value = stats.totalDoctors.toString(),
                    subtitle = "${stats.doctorsWithAssignments} with assignments",
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                StatCard(
                    title = "Institutions",
                    value = stats.totalInstitutions.toString(),
                    subtitle = "${stats.institutionsWithWards} with wards",
                    icon = Icons.Default.LocationOn,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            item {
                StatCard(
                    title = "Assignments",
                    value = stats.totalAssignments.toString(),
                    subtitle = "Active assignments",
                    icon = Icons.Default.Assignment,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            if (stats.totalWards > 0) {
                item {
                    StatCard(
                        title = "Wards",
                        value = stats.totalWards.toString(),
                        subtitle = "Total wards",
                        icon = Icons.Default.LocalHospital,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentDoctorsSection(
    doctors: List<Doctor>,
    onDoctorClick: (Int) -> Unit
) {
    Column {
        Text(
            text = "Recent Doctors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(doctors) { doctor ->
                RecentItemCard(
                    title = doctor.name,
                    subtitle = doctor.speciality,
                    onClick = { onDoctorClick(doctor.doctorId) }
                )
            }
        }
    }
}

@Composable
private fun RecentInstitutionsSection(
    institutions: List<Institution>,
    onInstitutionClick: (Int) -> Unit
) {
    Column {
        Text(
            text = "Recent Institutions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(institutions) { institution ->
                RecentItemCard(
                    title = institution.name,
                    subtitle = institution.areaBrick,
                    onClick = { onInstitutionClick(institution.institutionId) }
                )
            }
        }
    }
}

@Composable
private fun TopSpecialitiesSection(
    specialities: List<com.healthtracker.offline.ui.viewmodels.SpecialityCount>
) {
    Column {
        Text(
            text = "Top Specialities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                specialities.forEach { specialityCount ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = specialityCount.speciality,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = specialityCount.count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAreaBricksSection(
    areaBricks: List<com.healthtracker.offline.ui.viewmodels.AreaBrickCount>
) {
    Column {
        Text(
            text = "Top Area Bricks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                areaBricks.forEach { areaBrickCount ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = areaBrickCount.areaBrick,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = areaBrickCount.count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}