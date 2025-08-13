package com.healthtracker.offline.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.healthtracker.offline.data.dao.*
import com.healthtracker.offline.data.serialization.DataSerializer
import com.healthtracker.offline.data.serialization.DatabaseExport
import com.healthtracker.offline.data.serialization.ExportMetadata
import com.healthtracker.offline.data.file.FileManager
import com.healthtracker.offline.data.merge.DataMerger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced implementation of ExportImportRepository with comprehensive
 * serialization, validation, and file management capabilities.
 */
@Singleton
class ExportImportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val doctorDao: DoctorDao,
    private val institutionDao: InstitutionDao,
    private val wardDao: WardDao,
    private val doctorInstitutionDao: DoctorInstitutionDao,
    private val savedSearchFilterDao: SavedSearchFilterDao
) : ExportImportRepository {
    
    private val fileManager = FileManager(context)
    
    override suspend fun exportAllDataToJson(outputFile: File): Result<ExportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Collect all data
                val doctors = doctorDao.getAllDoctorsList()
                val institutions = institutionDao.getAllInstitutionsList()
                val wards = wardDao.getAllWardsList()
                val assignments = doctorInstitutionDao.getAllAssignmentsList()
                val savedFilters = savedSearchFilterDao.getAllSavedFiltersList()
                
                // Create export data structure
                val metadata = ExportMetadata(
                    exportDate = System.currentTimeMillis(),
                    appVersion = "1.0",
                    databaseVersion = 1,
                    exportType = "FULL",
                    deviceInfo = android.os.Build.MODEL
                )
                
                val exportData = DatabaseExport(
                    metadata = metadata,
                    doctors = doctors,
                    institutions = institutions,
                    wards = wards,
                    assignments = assignments,
                    savedFilters = savedFilters
                )
                
                // Serialize to JSON
                val jsonString = DataSerializer.serializeToJson(exportData)
                
                // Write to file with progress tracking
                val fileResult = fileManager.writeFile(outputFile, jsonString)
                
                if (!fileResult.success) {
                    return@withContext Result.failure(Exception(fileResult.error))
                }
                
                val exportTime = System.currentTimeMillis() - startTime
                
                Result.success(
                    ExportResult(
                        success = true,
                        filePath = outputFile.absolutePath,
                        fileSize = fileResult.fileSize,
                        recordCount = exportData.totalRecords,
                        format = ExportFormat.JSON,
                        exportTime = exportTime,
                        message = "Successfully exported ${exportData.totalRecords} records"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun exportAllDataToCsv(outputDirectory: File): Result<ExportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Ensure output directory exists
                if (!outputDirectory.exists()) {
                    outputDirectory.mkdirs()
                }
                
                // Collect all data
                val doctors = doctorDao.getAllDoctorsList()
                val institutions = institutionDao.getAllInstitutionsList()
                val wards = wardDao.getAllWardsList()
                val assignments = doctorInstitutionDao.getAllAssignmentsList()
                
                // Export each entity type to separate CSV files
                var totalSize = 0L
                
                // Export doctors
                if (doctors.isNotEmpty()) {
                    val doctorsFile = File(outputDirectory, "doctors.csv")
                    val doctorsCsv = DataSerializer.doctorsToCsv(doctors)
                    val result = fileManager.writeFile(doctorsFile, doctorsCsv)
                    if (result.success) {
                        totalSize += result.fileSize
                    }
                }
                
                // Export institutions
                if (institutions.isNotEmpty()) {
                    val institutionsFile = File(outputDirectory, "institutions.csv")
                    val institutionsCsv = DataSerializer.institutionsToCsv(institutions)
                    val result = fileManager.writeFile(institutionsFile, institutionsCsv)
                    if (result.success) {
                        totalSize += result.fileSize
                    }
                }
                
                // Export wards
                if (wards.isNotEmpty()) {
                    val wardsFile = File(outputDirectory, "wards.csv")
                    val wardsCsv = DataSerializer.wardsToCsv(wards)
                    val result = fileManager.writeFile(wardsFile, wardsCsv)
                    if (result.success) {
                        totalSize += result.fileSize
                    }
                }
                
                // Export assignments
                if (assignments.isNotEmpty()) {
                    val assignmentsFile = File(outputDirectory, "assignments.csv")
                    val assignmentsCsv = DataSerializer.assignmentsToCsv(assignments)
                    val result = fileManager.writeFile(assignmentsFile, assignmentsCsv)
                    if (result.success) {
                        totalSize += result.fileSize
                    }
                }
                
                val exportTime = System.currentTimeMillis() - startTime
                val totalRecords = doctors.size + institutions.size + wards.size + assignments.size
                
                Result.success(
                    ExportResult(
                        success = true,
                        filePath = outputDirectory.absolutePath,
                        fileSize = totalSize,
                        recordCount = totalRecords,
                        format = ExportFormat.CSV,
                        exportTime = exportTime,
                        message = "Successfully exported $totalRecords records to CSV files"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun exportDoctors(
        doctorIds: List<Int>?,
        format: ExportFormat,
        outputFile: File
    ): Result<ExportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val doctors = if (doctorIds != null) {
                    doctorIds.mapNotNull { doctorDao.getDoctorById(it) }
                } else {
                    doctorDao.getAllDoctorsList()
                }
                
                val content = when (format) {
                    ExportFormat.JSON -> DataSerializer.serializeDoctorsToJson(doctors)
                    ExportFormat.CSV -> DataSerializer.doctorsToCsv(doctors)
                }
                
                val fileResult = fileManager.writeFile(outputFile, content)
                
                if (!fileResult.success) {
                    return@withContext Result.failure(Exception(fileResult.error))
                }
                
                Result.success(
                    ExportResult(
                        success = true,
                        filePath = outputFile.absolutePath,
                        fileSize = fileResult.fileSize,
                        recordCount = doctors.size,
                        format = format,
                        exportTime = 0,
                        message = "Successfully exported ${doctors.size} doctors"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun exportInstitutions(
        institutionIds: List<Int>?,
        format: ExportFormat,
        outputFile: File
    ): Result<ExportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val institutions = if (institutionIds != null) {
                    institutionIds.mapNotNull { institutionDao.getInstitutionById(it) }
                } else {
                    institutionDao.getAllInstitutionsList()
                }
                
                val content = when (format) {
                    ExportFormat.JSON -> DataSerializer.serializeInstitutionsToJson(institutions)
                    ExportFormat.CSV -> DataSerializer.institutionsToCsv(institutions)
                }
                
                val fileResult = fileManager.writeFile(outputFile, content)
                
                if (!fileResult.success) {
                    return@withContext Result.failure(Exception(fileResult.error))
                }
                
                Result.success(
                    ExportResult(
                        success = true,
                        filePath = outputFile.absolutePath,
                        fileSize = fileResult.fileSize,
                        recordCount = institutions.size,
                        format = format,
                        exportTime = 0,
                        message = "Successfully exported ${institutions.size} institutions"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun exportAssignments(
        assignmentIds: List<Int>?,
        format: ExportFormat,
        outputFile: File
    ): Result<ExportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val assignments = if (assignmentIds != null) {
                    assignmentIds.mapNotNull { doctorInstitutionDao.getAssignmentById(it) }
                } else {
                    doctorInstitutionDao.getAllAssignmentsList()
                }
                
                val content = when (format) {
                    ExportFormat.JSON -> DataSerializer.serializeAssignmentsToJson(assignments)
                    ExportFormat.CSV -> DataSerializer.assignmentsToCsv(assignments)
                }
                
                val fileResult = fileManager.writeFile(outputFile, content)
                
                if (!fileResult.success) {
                    return@withContext Result.failure(Exception(fileResult.error))
                }
                
                Result.success(
                    ExportResult(
                        success = true,
                        filePath = outputFile.absolutePath,
                        fileSize = fileResult.fileSize,
                        recordCount = assignments.size,
                        format = format,
                        exportTime = 0,
                        message = "Successfully exported ${assignments.size} assignments"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun importFromJson(inputFile: File, strategy: ImportStrategy): Result<ImportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Validate and read file
                val validation = fileManager.validateFileFormat(inputFile, "json")
                if (!validation.isValid) {
                    return@withContext Result.failure(Exception("File validation failed: ${validation.errors.joinToString(", ")}"))
                }
                
                val readResult = fileManager.readFile(inputFile)
                if (!readResult.success) {
                    return@withContext Result.failure(Exception(readResult.error))
                }
                
                // Deserialize data
                val importData = DataSerializer.deserializeFromJson(readResult.content)
                
                // Import with merge strategy
                val result = performImport(importData, strategy)
                
                val importTime = System.currentTimeMillis() - startTime
                
                Result.success(result.copy(importTime = importTime))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun importFromCsv(inputDirectory: File, strategy: ImportStrategy): Result<ImportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                if (!inputDirectory.exists() || !inputDirectory.isDirectory) {
                    return@withContext Result.failure(Exception("Input directory does not exist"))
                }
                
                val csvFiles = inputDirectory.listFiles { _, name -> name.endsWith(".csv") }
                if (csvFiles.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("No CSV files found"))
                }
                
                var totalImported = 0
                var totalSkipped = 0
                val errors = mutableListOf<String>()
                
                // Process each CSV file
                csvFiles.forEach { file ->
                    try {
                        val readResult = fileManager.readFile(file)
                        if (readResult.success) {
                            when (file.nameWithoutExtension.lowercase()) {
                                "doctors" -> {
                                    val doctors = DataSerializer.csvToDoctors(readResult.content)
                                    val result = importDoctors(doctors, strategy)
                                    totalImported += result.first
                                    totalSkipped += result.second
                                }
                                "institutions" -> {
                                    val institutions = DataSerializer.csvToInstitutions(readResult.content)
                                    val result = importInstitutions(institutions, strategy)
                                    totalImported += result.first
                                    totalSkipped += result.second
                                }
                                "wards" -> {
                                    val wards = DataSerializer.csvToWards(readResult.content)
                                    val result = importWards(wards, strategy)
                                    totalImported += result.first
                                    totalSkipped += result.second
                                }
                                "assignments" -> {
                                    val assignments = DataSerializer.csvToAssignments(readResult.content)
                                    val result = importAssignments(assignments, strategy)
                                    totalImported += result.first
                                    totalSkipped += result.second
                                }
                            }
                        } else {
                            errors.add("Failed to read ${file.name}")
                        }
                    } catch (e: Exception) {
                        errors.add("Error processing ${file.name}: ${e.message}")
                    }
                }
                
                val importTime = System.currentTimeMillis() - startTime
                
                Result.success(
                    ImportResult(
                        success = errors.isEmpty(),
                        totalRecords = totalImported + totalSkipped,
                        importedRecords = totalImported,
                        skippedRecords = totalSkipped,
                        errorRecords = errors.size,
                        importTime = importTime,
                        errors = errors,
                        message = "Imported $totalImported records from ${csvFiles.size} files"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun validateImportFile(inputFile: File, format: ExportFormat): Result<ValidationResult> {
        return withContext(Dispatchers.IO) {
            try {
                val validation = fileManager.validateFileFormat(inputFile, format.extension)
                
                Result.success(
                    ValidationResult(
                        isValid = validation.isValid,
                        format = format,
                        recordCount = 0, // Would need content analysis
                        errors = validation.errors,
                        warnings = validation.warnings
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun previewImport(inputFile: File, format: ExportFormat): Result<ImportPreview> {
        return withContext(Dispatchers.IO) {
            try {
                val readResult = fileManager.readFile(inputFile)
                if (!readResult.success) {
                    return@withContext Result.failure(Exception(readResult.error))
                }
                
                when (format) {
                    ExportFormat.JSON -> {
                        val importData = DataSerializer.deserializeFromJson(readResult.content)
                        Result.success(
                            ImportPreview(
                                format = format,
                                totalRecords = importData.totalRecords,
                                doctorCount = importData.doctors.size,
                                institutionCount = importData.institutions.size,
                                wardCount = importData.wards.size,
                                assignmentCount = importData.assignments.size,
                                sampleData = mapOf(
                                    "doctors" to importData.doctors.take(3).map { it.name },
                                    "institutions" to importData.institutions.take(3).map { it.name }
                                )
                            )
                        )
                    }
                    ExportFormat.CSV -> {
                        val lines = readResult.content.lines().filter { it.isNotBlank() }
                        val recordCount = if (lines.isNotEmpty()) lines.size - 1 else 0
                        
                        Result.success(
                            ImportPreview(
                                format = format,
                                totalRecords = recordCount,
                                doctorCount = recordCount,
                                institutionCount = 0,
                                wardCount = 0,
                                assignmentCount = 0,
                                sampleData = mapOf("sample" to lines.take(4))
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun createBackup(backupFile: File): Result<ExportResult> {
        return exportAllDataToJson(backupFile)
    }
    
    override suspend fun restoreFromBackup(backupFile: File, clearExisting: Boolean): Result<ImportResult> {
        val strategy = if (clearExisting) ImportStrategy.REPLACE_ALL else ImportStrategy.SKIP_DUPLICATES
        return importFromJson(backupFile, strategy)
    }
    
    override fun getSupportedExportFormats(): List<ExportFormat> {
        return listOf(ExportFormat.JSON, ExportFormat.CSV)
    }
    
    override suspend fun getDefaultExportDirectory(): File {
        return fileManager.getExportDirectory()
    }
    
    override suspend fun cleanupTempFiles(): Result<Unit> {
        return try {
            fileManager.cleanupTempFiles()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private suspend fun performImport(importData: DatabaseExport, strategy: ImportStrategy): ImportResult {
        var totalImported = 0
        var totalSkipped = 0
        val errors = mutableListOf<String>()
        
        try {
            // Import doctors
            val doctorResult = importDoctors(importData.doctors, strategy)
            totalImported += doctorResult.first
            totalSkipped += doctorResult.second
            
            // Import institutions
            val institutionResult = importInstitutions(importData.institutions, strategy)
            totalImported += institutionResult.first
            totalSkipped += institutionResult.second
            
            // Import wards
            val wardResult = importWards(importData.wards, strategy)
            totalImported += wardResult.first
            totalSkipped += wardResult.second
            
            // Import assignments
            val assignmentResult = importAssignments(importData.assignments, strategy)
            totalImported += assignmentResult.first
            totalSkipped += assignmentResult.second
            
        } catch (e: Exception) {
            errors.add("Import error: ${e.message}")
        }
        
        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = importData.totalRecords,
            importedRecords = totalImported,
            skippedRecords = totalSkipped,
            errorRecords = errors.size,
            importTime = 0,
            errors = errors,
            message = if (errors.isEmpty()) "Successfully imported $totalImported records" else "Import completed with errors"
        )
    }
    
    private suspend fun importDoctors(doctors: List<com.healthtracker.offline.data.entities.Doctor>, strategy: ImportStrategy): Pair<Int, Int> {
        if (doctors.isEmpty()) return Pair(0, 0)
        
        val existing = doctorDao.getAllDoctorsList()
        val duplicates = DataMerger.detectDuplicateDoctors(existing, doctors)
        val toImport = duplicates.unique + DataMerger.mergeDoctors(duplicates.duplicates, strategy)
        
        return if (toImport.isNotEmpty()) {
            doctorDao.insertDoctors(toImport)
            Pair(toImport.size, duplicates.duplicateCount)
        } else {
            Pair(0, duplicates.duplicateCount)
        }
    }
    
    private suspend fun importInstitutions(institutions: List<com.healthtracker.offline.data.entities.Institution>, strategy: ImportStrategy): Pair<Int, Int> {
        if (institutions.isEmpty()) return Pair(0, 0)
        
        val existing = institutionDao.getAllInstitutionsList()
        val duplicates = DataMerger.detectDuplicateInstitutions(existing, institutions)
        val toImport = duplicates.unique + DataMerger.mergeInstitutions(duplicates.duplicates, strategy)
        
        return if (toImport.isNotEmpty()) {
            institutionDao.insertInstitutions(toImport)
            Pair(toImport.size, duplicates.duplicateCount)
        } else {
            Pair(0, duplicates.duplicateCount)
        }
    }
    
    private suspend fun importWards(wards: List<com.healthtracker.offline.data.entities.Ward>, strategy: ImportStrategy): Pair<Int, Int> {
        if (wards.isEmpty()) return Pair(0, 0)
        
        val existing = wardDao.getAllWardsList()
        val duplicates = DataMerger.detectDuplicateWards(existing, wards)
        val toImport = duplicates.unique + DataMerger.mergeWards(duplicates.duplicates, strategy)
        
        return if (toImport.isNotEmpty()) {
            wardDao.insertWards(toImport)
            Pair(toImport.size, duplicates.duplicateCount)
        } else {
            Pair(0, duplicates.duplicateCount)
        }
    }
    
    private suspend fun importAssignments(assignments: List<com.healthtracker.offline.data.entities.DoctorInstitution>, strategy: ImportStrategy): Pair<Int, Int> {
        if (assignments.isEmpty()) return Pair(0, 0)
        
        val existing = doctorInstitutionDao.getAllAssignmentsList()
        val duplicates = DataMerger.detectDuplicateAssignments(existing, assignments)
        val toImport = duplicates.unique + DataMerger.mergeAssignments(duplicates.duplicates, strategy)
        
        return if (toImport.isNotEmpty()) {
            doctorInstitutionDao.insertAssignments(toImport)
            Pair(toImport.size, duplicates.duplicateCount)
        } else {
            Pair(0, duplicates.duplicateCount)
        }
    }
}