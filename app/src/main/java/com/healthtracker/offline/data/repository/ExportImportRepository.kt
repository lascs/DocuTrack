package com.healthtracker.offline.data.repository

import java.io.File

/**
 * Repository interface for data export and import operations.
 * 
 * Provides functionality to export data to various formats and
 * import data from external sources with validation and merging.
 */
interface ExportImportRepository {
    
    // ========== EXPORT OPERATIONS ==========
    
    /**
     * Exports all data to JSON format
     * @param outputFile File to write the exported data
     * @return Result indicating success or error with file info
     */
    suspend fun exportAllDataToJson(outputFile: File): Result<ExportResult>
    
    /**
     * Exports all data to CSV format
     * @param outputDirectory Directory to write CSV files
     * @return Result indicating success or error with file info
     */
    suspend fun exportAllDataToCsv(outputDirectory: File): Result<ExportResult>
    
    /**
     * Exports filtered doctor data
     * @param doctorIds List of doctor IDs to export (null for all)
     * @param format Export format (JSON or CSV)
     * @param outputFile Output file or directory
     * @return Result indicating success or error
     */
    suspend fun exportDoctors(
        doctorIds: List<Int>? = null,
        format: ExportFormat,
        outputFile: File
    ): Result<ExportResult>
    
    /**
     * Exports filtered institution data
     * @param institutionIds List of institution IDs to export (null for all)
     * @param format Export format (JSON or CSV)
     * @param outputFile Output file or directory
     * @return Result indicating success or error
     */
    suspend fun exportInstitutions(
        institutionIds: List<Int>? = null,
        format: ExportFormat,
        outputFile: File
    ): Result<ExportResult>
    
    /**
     * Exports assignment data with details
     * @param assignmentIds List of assignment IDs to export (null for all)
     * @param format Export format (JSON or CSV)
     * @param outputFile Output file or directory
     * @return Result indicating success or error
     */
    suspend fun exportAssignments(
        assignmentIds: List<Int>? = null,
        format: ExportFormat,
        outputFile: File
    ): Result<ExportResult>
    
    // ========== IMPORT OPERATIONS ==========
    
    /**
     * Imports data from JSON file
     * @param inputFile JSON file to import
     * @param strategy Import strategy for handling duplicates
     * @return Result indicating success or error with import statistics
     */
    suspend fun importFromJson(
        inputFile: File,
        strategy: ImportStrategy = ImportStrategy.SKIP_DUPLICATES
    ): Result<ImportResult>
    
    /**
     * Imports data from CSV files
     * @param inputDirectory Directory containing CSV files
     * @param strategy Import strategy for handling duplicates
     * @return Result indicating success or error with import statistics
     */
    suspend fun importFromCsv(
        inputDirectory: File,
        strategy: ImportStrategy = ImportStrategy.SKIP_DUPLICATES
    ): Result<ImportResult>
    
    /**
     * Validates import file before processing
     * @param inputFile File to validate
     * @param format Expected format
     * @return Result indicating validation success or specific errors
     */
    suspend fun validateImportFile(inputFile: File, format: ExportFormat): Result<ValidationResult>
    
    /**
     * Previews import data without actually importing
     * @param inputFile File to preview
     * @param format File format
     * @return Result containing preview information
     */
    suspend fun previewImport(inputFile: File, format: ExportFormat): Result<ImportPreview>
    
    // ========== BACKUP OPERATIONS ==========
    
    /**
     * Creates a complete backup of all data
     * @param backupFile File to write backup
     * @return Result indicating success or error
     */
    suspend fun createBackup(backupFile: File): Result<ExportResult>
    
    /**
     * Restores data from a backup file
     * @param backupFile Backup file to restore
     * @param clearExisting Whether to clear existing data before restore
     * @return Result indicating success or error
     */
    suspend fun restoreFromBackup(
        backupFile: File,
        clearExisting: Boolean = false
    ): Result<ImportResult>
    
    // ========== FILE OPERATIONS ==========
    
    /**
     * Gets available export formats
     * @return List of supported export formats
     */
    fun getSupportedExportFormats(): List<ExportFormat>
    
    /**
     * Gets default export directory
     * @return Default directory for exports
     */
    suspend fun getDefaultExportDirectory(): File
    
    /**
     * Cleans up temporary export/import files
     * @return Result indicating cleanup success
     */
    suspend fun cleanupTempFiles(): Result<Unit>
}

/**
 * Enum for export formats
 */
enum class ExportFormat(val extension: String, val mimeType: String) {
    JSON("json", "application/json"),
    CSV("csv", "text/csv")
}

/**
 * Enum for import strategies
 */
enum class ImportStrategy {
    SKIP_DUPLICATES,    // Skip records that already exist
    UPDATE_DUPLICATES,  // Update existing records with new data
    REPLACE_ALL         // Clear existing data and import all
}

/**
 * Data class for export results
 */
data class ExportResult(
    val success: Boolean,
    val filePath: String,
    val fileSize: Long,
    val recordCount: Int,
    val format: ExportFormat,
    val exportTime: Long,
    val message: String = ""
)

/**
 * Data class for import results
 */
data class ImportResult(
    val success: Boolean,
    val totalRecords: Int,
    val importedRecords: Int,
    val skippedRecords: Int,
    val errorRecords: Int,
    val importTime: Long,
    val errors: List<String> = emptyList(),
    val message: String = ""
)

/**
 * Data class for validation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val format: ExportFormat,
    val recordCount: Int,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Data class for import preview
 */
data class ImportPreview(
    val format: ExportFormat,
    val totalRecords: Int,
    val doctorCount: Int,
    val institutionCount: Int,
    val wardCount: Int,
    val assignmentCount: Int,
    val sampleData: Map<String, List<String>> = emptyMap(),
    val potentialDuplicates: Int = 0
)