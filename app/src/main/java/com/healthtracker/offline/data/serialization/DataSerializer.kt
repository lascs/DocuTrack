package com.healthtracker.offline.data.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.healthtracker.offline.data.entities.*
import com.healthtracker.offline.data.models.SavedSearchFilter

/**
 * Data serialization utility for export/import operations.
 * 
 * Provides JSON and CSV serialization with proper data validation
 * and format conversion capabilities.
 */
object DataSerializer {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
    
    // ========== JSON SERIALIZATION ==========
    
    /**
     * Serializes complete database export to JSON
     */
    fun serializeToJson(exportData: DatabaseExport): String {
        return json.encodeToString(exportData)
    }
    
    /**
     * Deserializes JSON to database export data
     */
    fun deserializeFromJson(jsonString: String): DatabaseExport {
        return json.decodeFromString(jsonString)
    }
    
    /**
     * Serializes doctors list to JSON
     */
    fun serializeDoctorsToJson(doctors: List<Doctor>): String {
        return json.encodeToString(doctors)
    }
    
    /**
     * Deserializes JSON to doctors list
     */
    fun deserializeDoctorsFromJson(jsonString: String): List<Doctor> {
        return json.decodeFromString(jsonString)
    }
    
    /**
     * Serializes institutions list to JSON
     */
    fun serializeInstitutionsToJson(institutions: List<Institution>): String {
        return json.encodeToString(institutions)
    }
    
    /**
     * Deserializes JSON to institutions list
     */
    fun deserializeInstitutionsFromJson(jsonString: String): List<Institution> {
        return json.decodeFromString(jsonString)
    }
    
    /**
     * Serializes wards list to JSON
     */
    fun serializeWardsToJson(wards: List<Ward>): String {
        return json.encodeToString(wards)
    }
    
    /**
     * Deserializes JSON to wards list
     */
    fun deserializeWardsFromJson(jsonString: String): List<Ward> {
        return json.decodeFromString(jsonString)
    }
    
    /**
     * Serializes assignments list to JSON
     */
    fun serializeAssignmentsToJson(assignments: List<DoctorInstitution>): String {
        return json.encodeToString(assignments)
    }
    
    /**
     * Deserializes JSON to assignments list
     */
    fun deserializeAssignmentsFromJson(jsonString: String): List<DoctorInstitution> {
        return json.decodeFromString(jsonString)
    }
    
    // ========== CSV SERIALIZATION ==========
    
    /**
     * Converts doctors list to CSV format
     */
    fun doctorsToCsv(doctors: List<Doctor>): String {
        val csv = StringBuilder()
        csv.appendLine("doctorId,name,speciality,pmdcNumber,mobileNumber,qualifications")
        
        doctors.forEach { doctor ->
            val qualifications = doctor.qualifications.joinToString(";")
            csv.appendLine("${doctor.doctorId},\"${escapeCsv(doctor.name)}\",\"${escapeCsv(doctor.speciality)}\",\"${escapeCsv(doctor.pmdcNumber)}\",\"${escapeCsv(doctor.mobileNumber)}\",\"${escapeCsv(qualifications)}\"")
        }
        
        return csv.toString()
    }
    
    /**
     * Converts CSV to doctors list
     */
    fun csvToDoctors(csvString: String): List<Doctor> {
        val lines = csvString.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        
        // Skip header line
        return lines.drop(1).mapNotNull { line ->
            try {
                val fields = parseCsvLine(line)
                if (fields.size >= 6) {
                    Doctor(
                        doctorId = fields[0].toIntOrNull() ?: 0,
                        name = fields[1],
                        speciality = fields[2],
                        pmdcNumber = fields[3],
                        mobileNumber = fields[4],
                        qualifications = if (fields[5].isNotBlank()) fields[5].split(";") else emptyList()
                    )
                } else null
            } catch (e: Exception) {
                null // Skip invalid lines
            }
        }
    }
    
    /**
     * Converts institutions list to CSV format
     */
    fun institutionsToCsv(institutions: List<Institution>): String {
        val csv = StringBuilder()
        csv.appendLine("institutionId,name,msName,dmsName,areaBrick,segmentName,numberOfWards")
        
        institutions.forEach { institution ->
            csv.appendLine("${institution.institutionId},\"${escapeCsv(institution.name)}\",\"${escapeCsv(institution.msName)}\",\"${escapeCsv(institution.dmsName)}\",\"${escapeCsv(institution.areaBrick)}\",\"${escapeCsv(institution.segmentName)}\",${institution.numberOfWards}")
        }
        
        return csv.toString()
    }
    
    /**
     * Converts CSV to institutions list
     */
    fun csvToInstitutions(csvString: String): List<Institution> {
        val lines = csvString.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        
        return lines.drop(1).mapNotNull { line ->
            try {
                val fields = parseCsvLine(line)
                if (fields.size >= 7) {
                    Institution(
                        institutionId = fields[0].toIntOrNull() ?: 0,
                        name = fields[1],
                        msName = fields[2],
                        dmsName = fields[3],
                        areaBrick = fields[4],
                        segmentName = fields[5],
                        numberOfWards = fields[6].toIntOrNull() ?: 0
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Converts wards list to CSV format
     */
    fun wardsToCsv(wards: List<Ward>): String {
        val csv = StringBuilder()
        csv.appendLine("wardId,institutionId,wardName,opdDays,otDays")
        
        wards.forEach { ward ->
            val opdDays = ward.opdDays.joinToString(";")
            val otDays = ward.otDays.joinToString(";")
            csv.appendLine("${ward.wardId},${ward.institutionId},\"${escapeCsv(ward.wardName)}\",\"${escapeCsv(opdDays)}\",\"${escapeCsv(otDays)}\"")
        }
        
        return csv.toString()
    }
    
    /**
     * Converts CSV to wards list
     */
    fun csvToWards(csvString: String): List<Ward> {
        val lines = csvString.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        
        return lines.drop(1).mapNotNull { line ->
            try {
                val fields = parseCsvLine(line)
                if (fields.size >= 5) {
                    Ward(
                        wardId = fields[0].toIntOrNull() ?: 0,
                        institutionId = fields[1].toIntOrNull() ?: 0,
                        wardName = fields[2],
                        opdDays = if (fields[3].isNotBlank()) fields[3].split(";") else emptyList(),
                        otDays = if (fields[4].isNotBlank()) fields[4].split(";") else emptyList()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Converts assignments list to CSV format
     */
    fun assignmentsToCsv(assignments: List<DoctorInstitution>): String {
        val csv = StringBuilder()
        csv.appendLine("assignmentId,doctorId,institutionId,wardId,designation,dutyShift,dutyDays")
        
        assignments.forEach { assignment ->
            val dutyDays = assignment.dutyDays.joinToString(";")
            csv.appendLine("${assignment.assignmentId},${assignment.doctorId},${assignment.institutionId},${assignment.wardId ?: ""},\"${escapeCsv(assignment.designation)}\",\"${assignment.dutyShift.name}\",\"${escapeCsv(dutyDays)}\"")
        }
        
        return csv.toString()
    }
    
    /**
     * Converts CSV to assignments list
     */
    fun csvToAssignments(csvString: String): List<DoctorInstitution> {
        val lines = csvString.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        
        return lines.drop(1).mapNotNull { line ->
            try {
                val fields = parseCsvLine(line)
                if (fields.size >= 7) {
                    DoctorInstitution(
                        assignmentId = fields[0].toIntOrNull() ?: 0,
                        doctorId = fields[1].toIntOrNull() ?: 0,
                        institutionId = fields[2].toIntOrNull() ?: 0,
                        wardId = fields[3].toIntOrNull(),
                        designation = fields[4],
                        dutyShift = try { DutyShift.valueOf(fields[5]) } catch (e: Exception) { DutyShift.FULL_DAY },
                        dutyDays = if (fields[6].isNotBlank()) fields[6].split(";") else emptyList()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Escapes CSV special characters
     */
    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
    
    /**
     * Parses a CSV line handling quoted fields
     */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            
            when {
                char == '"' && !inQuotes -> {
                    inQuotes = true
                }
                char == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped quote
                        currentField.append('"')
                        i++ // Skip next quote
                    } else {
                        inQuotes = false
                    }
                }
                char == ',' && !inQuotes -> {
                    fields.add(currentField.toString())
                    currentField.clear()
                }
                else -> {
                    currentField.append(char)
                }
            }
            i++
        }
        
        // Add the last field
        fields.add(currentField.toString())
        
        return fields
    }
    
    /**
     * Validates JSON format
     */
    fun validateJsonFormat(jsonString: String): ValidationResult {
        return try {
            json.parseToJsonElement(jsonString)
            ValidationResult.valid()
        } catch (e: Exception) {
            ValidationResult.invalid(listOf("Invalid JSON format: ${e.message}"))
        }
    }
    
    /**
     * Validates CSV format
     */
    fun validateCsvFormat(csvString: String, expectedColumns: Int): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        val lines = csvString.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            errors.add("CSV file is empty")
            return ValidationResult.invalid(errors)
        }
        
        // Check header
        val headerFields = parseCsvLine(lines[0])
        if (headerFields.size != expectedColumns) {
            errors.add("Expected $expectedColumns columns, found ${headerFields.size}")
        }
        
        // Check data rows
        lines.drop(1).forEachIndexed { index, line ->
            val fields = parseCsvLine(line)
            if (fields.size != expectedColumns) {
                warnings.add("Row ${index + 2}: Expected $expectedColumns columns, found ${fields.size}")
            }
        }
        
        return when {
            errors.isNotEmpty() -> ValidationResult.invalid(errors)
            warnings.isNotEmpty() -> ValidationResult.withWarnings(warnings)
            else -> ValidationResult.valid()
        }
    }
}

/**
 * Complete database export data structure
 */
@Serializable
data class DatabaseExport(
    val metadata: ExportMetadata,
    val doctors: List<Doctor> = emptyList(),
    val institutions: List<Institution> = emptyList(),
    val wards: List<Ward> = emptyList(),
    val assignments: List<DoctorInstitution> = emptyList(),
    val savedFilters: List<SavedSearchFilter> = emptyList()
) {
    val totalRecords: Int
        get() = doctors.size + institutions.size + wards.size + assignments.size + savedFilters.size
}

/**
 * Export metadata information
 */
@Serializable
data class ExportMetadata(
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val databaseVersion: Int = 1,
    val exportType: String = "FULL",
    val deviceInfo: String = "",
    val checksum: String = ""
) {
    fun getFormattedDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(exportDate))
    }
}

/**
 * Validation result for serialization operations
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(errors: List<String>) = ValidationResult(false, errors)
        fun withWarnings(warnings: List<String>) = ValidationResult(true, warnings = warnings)
    }
}