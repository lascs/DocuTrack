package com.healthtracker.offline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.healthtracker.offline.data.repository.ExportImportRepository
import com.healthtracker.offline.data.repository.*
import java.io.File
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing export/import operations.
 * 
 * Handles data export, import, validation, and file management operations.
 */
@HiltViewModel
class ExportImportViewModel @Inject constructor(
    private val exportImportRepository: ExportImportRepository
) : ViewModel() {
    
    // ========== UI STATE ==========
    
    private val _uiState = MutableStateFlow(ExportImportUiState())
    val uiState: StateFlow<ExportImportUiState> = _uiState.asStateFlow()
    
    private val _exportProgress = MutableStateFlow(0)
    val exportProgress: StateFlow<Int> = _exportProgress.asStateFlow()
    
    private val _importProgress = MutableStateFlow(0)
    val importProgress: StateFlow<Int> = _importProgress.asStateFlow()
    
    private val _lastExportResult = MutableStateFlow<ExportResult?>(null)
    val lastExportResult: StateFlow<ExportResult?> = _lastExportResult.asStateFlow()
    
    private val _lastImportResult = MutableStateFlow<ImportResult?>(null)
    val lastImportResult: StateFlow<ImportResult?> = _lastImportResult.asStateFlow()
    
    private val _importPreview = MutableStateFlow<ImportPreview?>(null)
    val importPreview: StateFlow<ImportPreview?> = _importPreview.asStateFlow()
    
    // ========== EXPORT OPERATIONS ==========
    
    /**
     * Exports all data to JSON format
     */
    fun exportAllDataToJson(outputFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportError = null,
                exportMessage = null
            )
            _exportProgress.value = 0
            
            exportImportRepository.exportAllDataToJson(outputFile)
                .onSuccess { result ->
                    _lastExportResult.value = result
                    _exportProgress.value = 100
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = result.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = error.message ?: "Export failed"
                    )
                }
        }
    }
    
    /**
     * Exports all data to CSV format
     */
    fun exportAllDataToCsv(outputDirectory: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportError = null,
                exportMessage = null
            )
            _exportProgress.value = 0
            
            exportImportRepository.exportAllDataToCsv(outputDirectory)
                .onSuccess { result ->
                    _lastExportResult.value = result
                    _exportProgress.value = 100
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = result.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = error.message ?: "Export failed"
                    )
                }
        }
    }
    
    /**
     * Exports doctors data
     */
    fun exportDoctors(doctorIds: List<Int>?, format: ExportFormat, outputFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportError = null,
                exportMessage = null
            )
            
            exportImportRepository.exportDoctors(doctorIds, format, outputFile)
                .onSuccess { result ->
                    _lastExportResult.value = result
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = result.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = error.message ?: "Export failed"
                    )
                }
        }
    }
    
    /**
     * Exports institutions data
     */
    fun exportInstitutions(institutionIds: List<Int>?, format: ExportFormat, outputFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportError = null,
                exportMessage = null
            )
            
            exportImportRepository.exportInstitutions(institutionIds, format, outputFile)
                .onSuccess { result ->
                    _lastExportResult.value = result
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = result.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = error.message ?: "Export failed"
                    )
                }
        }
    }
    
    /**
     * Exports assignments data
     */
    fun exportAssignments(assignmentIds: List<Int>?, format: ExportFormat, outputFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportError = null,
                exportMessage = null
            )
            
            exportImportRepository.exportAssignments(assignmentIds, format, outputFile)
                .onSuccess { result ->
                    _lastExportResult.value = result
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = result.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = error.message ?: "Export failed"
                    )
                }
        }
    }
    
    // ========== IMPORT OPERATIONS ==========
    
    /**
     * Imports data from JSON file
     */
    fun importFromJson(inputFile: File, strategy: ImportStrategy) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                importError = null,
                importMessage = null
            )
            _importProgress.value = 0
            
            exportImportRepository.importFromJson(inputFile, strategy)
                .onSuccess { result ->
                    _lastImportResult.value = result
                    _importProgress.value = 100
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importMessage = result.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importError = error.message ?: "Import failed"
                    )
                }
        }
    }
    
    /**
     * Imports data from CSV files
     */
    fun importFromCsv(inputDirectory: File, strategy: ImportStrategy) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                importError = null,
                importMessage = null
            )
            _importProgress.value = 0
            
            exportImportRepository.importFromCsv(inputDirectory, strategy)
                .onSuccess { result ->
                    _lastImportResult.value = result
                    _importProgress.value = 100
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importMessage = result.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importError = error.message ?: "Import failed"
                    )
                }
        }
    }
    
    // ========== VALIDATION AND PREVIEW ==========
    
    /**
     * Validates an import file
     */
    fun validateImportFile(inputFile: File, format: ExportFormat) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isValidating = true,
                validationError = null
            )
            
            exportImportRepository.validateImportFile(inputFile, format)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        validationResult = result
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        validationError = error.message ?: "Validation failed"
                    )
                }
        }
    }
    
    /**
     * Previews import data
     */
    fun previewImport(inputFile: File, format: ExportFormat) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isValidating = true,
                validationError = null
            )
            
            exportImportRepository.previewImport(inputFile, format)
                .onSuccess { preview ->
                    _importPreview.value = preview
                    _uiState.value = _uiState.value.copy(
                        isValidating = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        validationError = error.message ?: "Preview failed"
                    )
                }
        }
    }
    
    // ========== BACKUP OPERATIONS ==========
    
    /**
     * Creates a backup of all data
     */
    fun createBackup(backupFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportError = null,
                exportMessage = null
            )
            
            exportImportRepository.createBackup(backupFile)
                .onSuccess { result ->
                    _lastExportResult.value = result
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = "Backup created successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = error.message ?: "Backup failed"
                    )
                }
        }
    }
    
    /**
     * Restores data from backup
     */
    fun restoreFromBackup(backupFile: File, clearExisting: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                importError = null,
                importMessage = null
            )
            
            exportImportRepository.restoreFromBackup(backupFile, clearExisting)
                .onSuccess { result ->
                    _lastImportResult.value = result
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importMessage = "Data restored successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importError = error.message ?: "Restore failed"
                    )
                }
        }
    }
    
    // ========== FILE MANAGEMENT ==========
    
    /**
     * Gets the default export directory
     */
    fun getDefaultExportDirectory() {
        viewModelScope.launch {
            try {
                val directory = exportImportRepository.getDefaultExportDirectory()
                _uiState.value = _uiState.value.copy(
                    defaultExportDirectory = directory
                )
            } catch (e: Exception) {
                // Ignore error, use default
            }
        }
    }
    
    /**
     * Gets supported export formats
     */
    fun getSupportedFormats(): List<ExportFormat> {
        return exportImportRepository.getSupportedExportFormats()
    }
    
    /**
     * Cleans up temporary files
     */
    fun cleanupTempFiles() {
        viewModelScope.launch {
            exportImportRepository.cleanupTempFiles()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Temporary files cleaned up"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Cleanup failed"
                    )
                }
        }
    }
    
    // ========== STATE MANAGEMENT ==========
    
    /**
     * Clears export results and progress
     */
    fun clearExportState() {
        _lastExportResult.value = null
        _exportProgress.value = 0
        _uiState.value = _uiState.value.copy(
            exportError = null,
            exportMessage = null
        )
    }
    
    /**
     * Clears import results and progress
     */
    fun clearImportState() {
        _lastImportResult.value = null
        _importProgress.value = 0
        _importPreview.value = null
        _uiState.value = _uiState.value.copy(
            importError = null,
            importMessage = null,
            validationResult = null,
            validationError = null
        )
    }
    
    /**
     * Clears all messages and errors
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            message = null,
            exportError = null,
            exportMessage = null,
            importError = null,
            importMessage = null,
            validationError = null
        )
    }
    
    /**
     * Gets formatted file size
     */
    fun formatFileSize(bytes: Long): String {
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
     * Gets formatted duration
     */
    fun formatDuration(milliseconds: Long): String {
        return when {
            milliseconds < 1000 -> "${milliseconds}ms"
            milliseconds < 60000 -> "${milliseconds / 1000}s"
            else -> "${milliseconds / 60000}m ${(milliseconds % 60000) / 1000}s"
        }
    }
}

/**
 * UI state for export/import operations
 */
data class ExportImportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isValidating: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val exportError: String? = null,
    val exportMessage: String? = null,
    val importError: String? = null,
    val importMessage: String? = null,
    val validationError: String? = null,
    val validationResult: ValidationResult? = null,
    val defaultExportDirectory: File? = null
)