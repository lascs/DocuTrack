package com.healthtracker.offline.ui.screens.institutions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.healthtracker.offline.ui.viewmodels.InstitutionViewModel
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward

/**
 * Screen showing detailed information about a specific institution.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionDetailScreen(
    institutionId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToAddWard: () -> Unit,
    onNavigateToEditWard: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstitutionViewModel = hiltViewModel()
) {
    val uiState by viewModel.institutionDetailUiState.collectAsStateWithLifecycle()
    val institution by viewModel.selectedInstitution.collectAsStateWithLifecycle()
    val wards by viewModel.institutionWards.collectAsStateWithLifecycle()
    val assignedDoctors by viewModel.assignedDoctors.collectAsStateWithLifecycle()
    
    LaunchedEffect(institutionId) {
        viewModel.loadInstitutionDetail(institutionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = institution?.name ?: "Institution Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Institution"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddWard
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Ward"
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error,
                    onRetry = { viewModel.loadInstitutionDetail(institutionId) },
                    onNavigateBack = onNavigateBack,
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            institution != null -> {
                InstitutionDetailContent(
                    institution = institution,
                    wards = wards,
                    assignedDoctors = assignedDoctors,
                    onNavigateToAddWard = onNavigateToAddWard,
                    onNavigateToEditWard = onNavigateToEditWard,
                    onDeleteWard = { wardId ->
                        viewModel.deleteWard(wardId)
                    },
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
    
    // Handle delete confirmation
    uiState.showDeleteConfirmation?.let { ward ->
        DeleteWardConfirmationDialog(
            ward = ward,
            onConfirm = {
                viewModel.confirmDeleteWard()
            },
            onDismiss = {
                viewModel.cancelDeleteWard()
            }
        )
    }
    
    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
}

@Composable
private fun InstitutionDetailContent(
    institution: Institution,
    wards: List<Ward>,
    assignedDoctors: List<InstitutionDoctorSummary>,
    onNavigateToAddWard: () -> Unit,
    onNavigateToEditWard: (Int) -> Unit,
    onDeleteWard: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Institution Information Card
        item {
            InstitutionInfoCard(institution = institution)
        }
        
        // Quick Stats Card
        item {
            QuickStatsCard(
                wardsCount = wards.size,
                doctorsCount = assignedDoctors.size
            )
        }
        
        // Wards Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wards (${wards.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onNavigateToAddWard) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Ward")
                }
            }
        }
        
        if (wards.isEmpty()) {
            item {
                NoWardsCard(onAddWard = onNavigateToAddWard)
            }
        } else {
            items(
                items = wards,
                key = { it.wardId }
            ) { ward ->
                WardCard(
                    ward = ward,
                    onEditClick = { onNavigateToEditWard(ward.wardId) },
                    onDeleteClick = { onDeleteWard(ward.wardId) }
                )
            }
        }
        
        // Assigned Doctors Section
        if (assignedDoctors.isNotEmpty()) {
            item {
                Text(
                    text = "Assigned Doctors (${assignedDoctors.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(
                items = assignedDoctors,
                key = { it.doctorId }
            ) { doctor ->
                AssignedDoctorCard(doctor = doctor)
            }
        }
    }
}

@Composable
private fun InstitutionInfoCard(
    institution: Institution,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = institution.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = institution.areaBrick,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Management Information
            if (institution.msName.isNotBlank()) {
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Medical Superintendent",
                    value = institution.msName
                )
            }
            
            if (institution.dmsName.isNotBlank()) {
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Deputy Medical Superintendent",
                    value = institution.dmsName
                )
            }
            
            if (institution.segmentName.isNotBlank()) {
                InfoRow(
                    icon = Icons.Default.Category,
                    label = "Segment",
                    value = institution.segmentName
                )
            }
            
            InfoRow(
                icon = Icons.Default.LocalHospital,
                label = "Number of Wards",
                value = institution.numberOfWards.toString()
            )
        }
    }
}

@Composable
private fun QuickStatsCard(
    wardsCount: Int,
    doctorsCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.LocalHospital,
                label = "Wards",
                value = wardsCount.toString()
            )
            
            StatItem(
                icon = Icons.Default.People,
                label = "Doctors",
                value = doctorsCount.toString()
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun WardCard(
    ward: Ward,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = ward.wardName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (ward.opdDays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "OPD Days: ${ward.opdDays.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (ward.otDays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "OT Days: ${ward.otDays.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Ward",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Ward",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignedDoctorCard(
    doctor: InstitutionDoctorSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = doctor.speciality,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (doctor.designation.isNotBlank()) {
                    Text(
                        text = doctor.designation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (doctor.wardName.isNotBlank()) {
                    Text(
                        text = "Ward: ${doctor.wardName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NoWardsCard(
    onAddWard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No wards yet",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Add wards to organize this institution",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(onClick = onAddWard) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Ward")
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Error loading institution",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Go Back")
                }
                
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun DeleteWardConfirmationDialog(
    ward: Ward,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Delete Ward?")
        },
        text = {
            Text("Are you sure you want to delete the ward \"${ward.wardName}\"? This will also remove any doctor assignments to this ward. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Data class representing a summary of a doctor assigned to an institution.
 */
data class InstitutionDoctorSummary(
    val doctorId: Int,
    val name: String,
    val speciality: String,
    val designation: String,
    val wardName: String
)