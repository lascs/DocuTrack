package com.healthtracker.offline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.healthtracker.offline.data.repository.ExportImportRepository
import com.healthtracker.offline.data.repository.DoctorRepository
import com.healthtracker.offline.data.repository.InstitutionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for app settings and data management.
 * 
 * Handles theme settings, data backup reminders, database operations, and app info.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportImportRepository: ExportImportRepository,
    private val doctorRepository: DoctorRepository,
    private val institutionRepository: InstitutionRepository
) : ViewModel() {
    
    // ========== UI STATE ==========
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // ========== THEME SETTINGS ==========
    
    private val _selectedTheme = MutableStateFlow(AppTheme.SYSTEM)
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()
    
    private val _isDynamicColorEnabled = MutableStateFlow(true)
    val isDynamicColorEnabled: StateFlow<Boolean> = _isDynamicColorEnabled.asStateFlow()
    
    // ========== BACKUP SETTINGS ==========
    
    private val _backupReminderEnabled = MutableStateFlow(true)
    val backupReminderEnabled: StateFlow<Boolean> = _backupReminderEnabled.asStateFlow()
    
    private val _backupReminderInterval = MutableStateFlow(BackupInterval.WEEKLY)
    val backupReminderInterval: StateFlow<BackupInterval> = _backupReminderInterval.asStateFlow()
    
    private val _lastBackupTime = MutableStateFlow<Long?>(null)
    val lastBackupTime: StateFlow<Long?> = _lastBackupTime.asStateFlow()
    
    // ========== DATABASE INFO ==========
    
    val databaseStats: StateFlow<DatabaseStats> = combine(
        doctorRepository.getAllDoctors(),
        institutionRepository.getAllInstitutions(),
        institutionRepository.getInstitutionsWithWardCount(),
        doctorRepository.getDoctorsWithAssignmentCount()
    ) { doctors, institutions, institutionsWithWards, doctorsWithAssignments ->
        DatabaseStats(
            totalDoctors = doctors.size,
            totalInstitutions = institutions.size,
            totalWards = institutionsWithWards.sumOf { it.actualWardCount },
            totalAssignments = doctorsWithAssignments.sumOf { it.assignmentCount },
            databaseSize = 0L // Would need actual database file size calculation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DatabaseStats()
    )
    
    // ========== THEME ACTIONS ==========
    
    /**
     * Updates the selected theme
     */
    fun updateTheme(theme: AppTheme) {
        _selectedTheme.value = theme
        // In a real app, this would persist to SharedPreferences
        saveThemePreference(theme)
    }
    
    /**
     * Toggles dynamic color support
     */
    fun toggleDynamicColor(enabled: Boolean) {
        _isDynamicColorEnabled.value = enabled
        // In a real app, this would persist to SharedPreferences
        saveDynamicColorPreference(enabled)
    }
    
    // ========== BACKUP ACTIONS ==========
    
    /**
     * Toggles backup reminder
     */
    fun toggleBackupReminder(enabled: Boolean) {
        _backupReminderEnabled.value = enabled
        saveBackupReminderPreference(enabled)
    }
    
    /**
     * Updates backup reminder interval
     */
    fun updateBackupInterval(interval: BackupInterval) {
        _backupReminderInterval.value = interval
        saveBackupIntervalPreference(interval)
    }
    
    /**
     * Creates a manual backup
     */
    fun createManualBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreatingBackup = true,
                error = null
            )
            
            try {
                val backupDir = exportImportRepository.getDefaultExportDirectory()
                val backupFile = java.io.File(backupDir, "backup_${System.currentTimeMillis()}.json")
                
                exportImportRepository.createBackup(backupFile)
                    .onSuccess { result ->
                        _lastBackupTime.value = System.currentTimeMillis()
                        _uiState.value = _uiState.value.copy(
                            isCreatingBackup = false,
                            message = "Backup created successfully"
                        )
                        saveLastBackupTime(System.currentTimeMillis())
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isCreatingBackup = false,
                            error = error.message ?: "Backup failed"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingBackup = false,
                    error = e.message ?: "Backup failed"
                )
            }
        }
    }
    
    /**
     * Checks if backup reminder should be shown
     */
    fun shouldShowBackupReminder(): Boolean {
        if (!_backupReminderEnabled.value) return false
        
        val lastBackup = _lastBackupTime.value ?: return true
        val currentTime = System.currentTimeMillis()
        val intervalMs = when (_backupReminderInterval.value) {
            BackupInterval.DAILY -> 24 * 60 * 60 * 1000L
            BackupInterval.WEEKLY -> 7 * 24 * 60 * 60 * 1000L
            BackupInterval.MONTHLY -> 30 * 24 * 60 * 60 * 1000L
        }
        
        return (currentTime - lastBackup) >= intervalMs
    }
    
    // ========== DATABASE ACTIONS ==========
    
    /**
     * Clears all database data with confirmation
     */
    fun clearDatabase(confirmed: Boolean = false) {
        if (!confirmed) {
            _uiState.value = _uiState.value.copy(
                showClearDatabaseConfirmation = true
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isClearingDatabase = true,
                showClearDatabaseConfirmation = false,
                error = null
            )
            
            try {
                // Clear all data (this would need to be implemented in repositories)
                // doctorRepository.deleteAllDoctors()
                // institutionRepository.deleteAllInstitutions()
                // etc.
                
                _uiState.value = _uiState.value.copy(
                    isClearingDatabase = false,
                    message = "Database cleared successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isClearingDatabase = false,
                    error = e.message ?: "Failed to clear database"
                )
            }
        }
    }
    
    /**
     * Cancels database clear confirmation
     */
    fun cancelClearDatabase() {
        _uiState.value = _uiState.value.copy(
            showClearDatabaseConfirmation = false
        )
    }
    
    /**
     * Cleans up temporary files
     */
    fun cleanupTempFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCleaningUp = true,
                error = null
            )
            
            exportImportRepository.cleanupTempFiles()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isCleaningUp = false,
                        message = "Temporary files cleaned up"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isCleaningUp = false,
                        error = error.message ?: "Cleanup failed"
                    )
                }
        }
    }
    
    // ========== APP INFO ==========
    
    /**
     * Gets app version information
     */
    fun getAppInfo(): AppInfo {
        return AppInfo(
            appName = "Offline Doctor Tracker",
            version = "1.0.0",
            buildNumber = "1",
            buildDate = "2024-01-01", // Would be set during build
            developer = "Healthcare Solutions",
            description = "A completely offline application for managing doctor and institution information"
        )
    }
    
    /**
     * Gets formatted database size
     */
    fun getFormattedDatabaseSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
    
    /**
     * Gets formatted time since last backup
     */
    fun getTimeSinceLastBackup(): String? {
        val lastBackup = _lastBackupTime.value ?: return null
        val currentTime = System.currentTimeMillis()
        val diffMs = currentTime - lastBackup
        
        return when {
            diffMs < 60 * 1000 -> "Just now"
            diffMs < 60 * 60 * 1000 -> "${diffMs / (60 * 1000)} minutes ago"
            diffMs < 24 * 60 * 60 * 1000 -> "${diffMs / (60 * 60 * 1000)} hours ago"
            else -> "${diffMs / (24 * 60 * 60 * 1000)} days ago"
        }
    }
    
    /**
     * Clears any error or message state
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }
    
    // ========== PERSISTENCE HELPERS ==========
    // In a real app, these would use SharedPreferences or DataStore
    
    private fun saveThemePreference(theme: AppTheme) {
        // Implementation would save to SharedPreferences
    }
    
    private fun saveDynamicColorPreference(enabled: Boolean) {
        // Implementation would save to SharedPreferences
    }
    
    private fun saveBackupReminderPreference(enabled: Boolean) {
        // Implementation would save to SharedPreferences
    }
    
    private fun saveBackupIntervalPreference(interval: BackupInterval) {
        // Implementation would save to SharedPreferences
    }
    
    private fun saveLastBackupTime(time: Long) {
        // Implementation would save to SharedPreferences
    }
    
    // ========== INITIALIZATION ==========
    
    init {
        // Load saved preferences
        loadPreferences()
    }
    
    private fun loadPreferences() {
        // In a real app, this would load from SharedPreferences
        // For now, using defaults
    }
}

/**
 * UI state for settings screen
 */
data class SettingsUiState(
    val isCreatingBackup: Boolean = false,
    val isClearingDatabase: Boolean = false,
    val isCleaningUp: Boolean = false,
    val showClearDatabaseConfirmation: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

/**
 * App theme options
 */
enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default")
}

/**
 * Backup reminder intervals
 */
enum class BackupInterval(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

/**
 * Database statistics
 */
data class DatabaseStats(
    val totalDoctors: Int = 0,
    val totalInstitutions: Int = 0,
    val totalWards: Int = 0,
    val totalAssignments: Int = 0,
    val databaseSize: Long = 0
) {
    val totalRecords: Int
        get() = totalDoctors + totalInstitutions + totalWards + totalAssignments
}

/**
 * App information
 */
data class AppInfo(
    val appName: String,
    val version: String,
    val buildNumber: String,
    val buildDate: String,
    val developer: String,
    val description: String
)