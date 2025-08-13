package com.healthtracker.offline.ui.screens.settings

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
import com.healthtracker.offline.ui.viewmodels.SettingsViewModel

/**
 * Main settings screen with app preferences and management options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToTheme: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
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
            // App Preferences Section
            item {
                Text(
                    text = "App Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                AppPreferencesCard(
                    settings = appSettings,
                    onThemeClick = onNavigateToTheme,
                    onNotificationsToggle = { enabled ->
                        viewModel.updateNotificationSettings(enabled)
                    },
                    onAutoBackupToggle = { enabled ->
                        viewModel.updateAutoBackupSettings(enabled)
                    }
                )
            }
            
            // Data Management Section
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                DataManagementCard(
                    onBackupClick = onNavigateToBackup,
                    onClearDataClick = {
                        viewModel.showClearDataConfirmation()
                    },
                    lastBackupDate = appSettings.lastBackupDate
                )
            }
            
            // App Information Section
            item {
                Text(
                    text = "App Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                AppInfoCard(
                    onAboutClick = onNavigateToAbout,
                    appVersion = appSettings.appVersion
                )
            }
            
            // Database Statistics
            item {
                DatabaseStatsCard(
                    stats = appSettings.databaseStats
                )
            }
        }
    }
    
    // Clear data confirmation dialog
    if (uiState.showClearDataDialog) {
        ClearDataConfirmationDialog(
            onConfirm = {
                viewModel.clearAllData()
            },
            onDismiss = {
                viewModel.dismissClearDataDialog()
            }
        )
    }
    
    // Handle messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar
            viewModel.clearMessage()
        }
    }
}

@Composable
private fun AppPreferencesCard(
    settings: AppSettings,
    onThemeClick: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onAutoBackupToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Setting
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = settings.themeName,
                onClick = onThemeClick,
                showChevron = true
            )
            
            // Notifications Setting
            SettingsItemWithSwitch(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Enable app notifications",
                checked = settings.notificationsEnabled,
                onCheckedChange = onNotificationsToggle
            )
            
            // Auto Backup Setting
            SettingsItemWithSwitch(
                icon = Icons.Default.CloudSync,
                title = "Auto Backup",
                subtitle = "Automatic daily backup reminders",
                checked = settings.autoBackupEnabled,
                onCheckedChange = onAutoBackupToggle
            )
        }
    }
}

@Composable
private fun DataManagementCard(
    onBackupClick: () -> Unit,
    onClearDataClick: () -> Unit,
    lastBackupDate: String?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup & Restore
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                subtitle = lastBackupDate?.let { "Last backup: $it" } ?: "No backups yet",
                onClick = onBackupClick,
                showChevron = true
            )
            
            // Clear All Data
            SettingsItem(
                icon = Icons.Default.DeleteSweep,
                title = "Clear All Data",
                subtitle = "Remove all doctors, institutions, and assignments",
                onClick = onClearDataClick,
                showChevron = false,
                textColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AppInfoCard(
    onAboutClick: () -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // About
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App information and licenses",
                onClick = onAboutClick,
                showChevron = true
            )
            
            // Version
            SettingsItem(
                icon = Icons.Default.AppRegistration,
                title = "Version",
                subtitle = appVersion,
                onClick = { },
                showChevron = false
            )
        }
    }
}

@Composable
private fun DatabaseStatsCard(
    stats: DatabaseStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Database Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DatabaseStatItem(
                    label = "Doctors",
                    value = stats.doctorsCount.toString()
                )
                
                DatabaseStatItem(
                    label = "Institutions",
                    value = stats.institutionsCount.toString()
                )
                
                DatabaseStatItem(
                    label = "Assignments",
                    value = stats.assignmentsCount.toString()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Database size: ${stats.databaseSize}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DatabaseStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showChevron: Boolean,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
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
                modifier = Modifier.size(24.dp),
                tint = textColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsItemWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ClearDataConfirmationDialog(
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
            Text("Clear All Data?")
        },
        text = {
            Text(
                "This will permanently delete all doctors, institutions, wards, and assignments from the database. This action cannot be undone.\n\nConsider creating a backup first."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear Data")
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
 * Data classes for settings
 */
data class AppSettings(
    val themeName: String = "System Default",
    val notificationsEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = false,
    val lastBackupDate: String? = null,
    val appVersion: String = "1.0.0",
    val databaseStats: DatabaseStats = DatabaseStats()
)

data class DatabaseStats(
    val doctorsCount: Int = 0,
    val institutionsCount: Int = 0,
    val wardsCount: Int = 0,
    val assignmentsCount: Int = 0,
    val databaseSize: String = "0 KB"
)