package com.healthtracker.offline.ui.screens.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthtracker.offline.ui.viewmodels.ExportImportViewModel

/**
 * Main Export/Import screen for data management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    onNavigateToExport: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToBackup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExportImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dataStats by viewModel.dataStats.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadDataStats()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Export & Import")
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Overview Card
            item {
                DataOverviewCard(
                    stats = dataStats,
                    isLoading = uiState.isLoading
                )
            }
            
            // Export Section
            item {
                Text(
                    text = "Export Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                ExportOptionsCard(
                    onExportAll = onNavigateToExport,
                    onExportDoctors = { /* Export doctors only */ },
                    onExportInstitutions = { /* Export institutions only */ },
                    onQuickExport = {
                        viewModel.quickExportAll()
                    }
                )
            }
            
            // Import Section
            item {
                Text(
                    text = "Import Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                ImportOptionsCard(
                    onImportFile = onNavigateToImport,
                    onImportFromBackup = onNavigateToBackup
                )
            }
            
            // Backup & Restore Section
            item {
                Text(
                    text = "Backup & Restore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                BackupOptionsCard(
                    onCreateBackup = {
                        viewModel.createBackup()
                    },
                    onRestoreBackup = onNavigateToBackup,
                    lastBackupDate = uiState.lastBackupDate
                )
            }
            
            // Recent Operations
            if (uiState.recentOperations.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Operations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    RecentOperationsCard(
                        operations = uiState.recentOperations
                    )
                }
            }
        }
    }
    
    // Handle loading state
    if (uiState.isProcessing) {
        ProcessingDialog(
            operation = uiState.currentOperation,
            progress = uiState.progress
        )
    }
    
    // Handle success/error messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar
            viewModel.clearMessage()
        }
    }
}

@Composable
private fun DataOverviewCard(
    stats: DataStats,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Data Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DataStatItem(
                        label = "Doctors",
                        value = stats.doctorsCount.toString(),
                        icon = Icons.Default.Person
                    )
                    
                    DataStatItem(
                        label = "Institutions",
                        value = stats.institutionsCount.toString(),
                        icon = Icons.Default.LocationOn
                    )
                    
                    DataStatItem(
                        label = "Assignments",
                        value = stats.assignmentsCount.toString(),
                        icon = Icons.Default.Assignment
                    )
                }
            }
        }
    }
}

@Composable
private fun DataStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ExportOptionsCard(
    onExportAll: () -> Unit,
    onExportDoctors: () -> Unit,
    onExportInstitutions: () -> Unit,
    onQuickExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExportOptionButton(
                title = "Export All Data",
                description = "Export complete database to JSON/CSV",
                icon = Icons.Default.FileDownload,
                onClick = onExportAll
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportDoctors,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Doctors Only")
                }
                
                OutlinedButton(
                    onClick = onExportInstitutions,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Institutions Only")
                }
            }
            
            TextButton(
                onClick = onQuickExport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Export (JSON)")
            }
        }
    }
}

@Composable
private fun ImportOptionsCard(
    onImportFile: () -> Unit,
    onImportFromBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExportOptionButton(
                title = "Import from File",
                description = "Import data from JSON or CSV file",
                icon = Icons.Default.FileUpload,
                onClick = onImportFile
            )
            
            ExportOptionButton(
                title = "Import from Backup",
                description = "Restore from previous backup",
                icon = Icons.Default.Restore,
                onClick = onImportFromBackup
            )
        }
    }
}

@Composable
private fun BackupOptionsCard(
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    lastBackupDate: String?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExportOptionButton(
                title = "Create Backup",
                description = "Create a complete backup of your data",
                icon = Icons.Default.Backup,
                onClick = onCreateBackup
            )
            
            ExportOptionButton(
                title = "Restore from Backup",
                description = "Restore data from a backup file",
                icon = Icons.Default.SettingsBackupRestore,
                onClick = onRestoreBackup
            )
            
            if (lastBackupDate != null) {
                Text(
                    text = "Last backup: $lastBackupDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExportOptionButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentOperationsCard(
    operations: List<RecentOperation>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            operations.forEach { operation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (operation.type) {
                            OperationType.EXPORT -> Icons.Default.FileDownload
                            OperationType.IMPORT -> Icons.Default.FileUpload
                            OperationType.BACKUP -> Icons.Default.Backup
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (operation.success) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = operation.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = operation.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (operation.success) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingDialog(
    operation: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text("Processing...")
        },
        text = {
            Column {
                Text(operation)
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                if (progress > 0) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * Data classes for the export/import screen
 */
data class DataStats(
    val doctorsCount: Int = 0,
    val institutionsCount: Int = 0,
    val wardsCount: Int = 0,
    val assignmentsCount: Int = 0
)

data class RecentOperation(
    val type: OperationType,
    val description: String,
    val timestamp: String,
    val success: Boolean
)

enum class OperationType {
    EXPORT, IMPORT, BACKUP
}