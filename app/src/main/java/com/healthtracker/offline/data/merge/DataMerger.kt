package com.healthtracker.offline.data.merge

import com.healthtracker.offline.data.entities.*
import com.healthtracker.offline.data.models.SavedSearchFilter
import com.healthtracker.offline.data.repository.ImportStrategy

/**
 * Data merging utility for handling duplicate detection and resolution
 * during import operations.
 */
object DataMerger {
    
    // ========== DUPLICATE DETECTION ==========
    
    /**
     * Detects duplicate doctors based on PMDC number
     */
    fun detectDuplicateDoctors(
        existingDoctors: List<Doctor>,
        newDoctors: List<Doctor>
    ): DuplicateDetectionResult<Doctor> {
        val duplicates = mutableListOf<DuplicatePair<Doctor>>()
        val unique = mutableListOf<Doctor>()
        
        newDoctors.forEach { newDoctor ->
            val existing = existingDoctors.find { it.pmdcNumber == newDoctor.pmdcNumber }
            if (existing != null) {
                duplicates.add(DuplicatePair(existing, newDoctor))
            } else {
                unique.add(newDoctor)
            }
        }
        
        return DuplicateDetectionResult(duplicates, unique)
    }
    
    /**
     * Detects duplicate institutions based on name and area brick
     */
    fun detectDuplicateInstitutions(
        existingInstitutions: List<Institution>,
        newInstitutions: List<Institution>
    ): DuplicateDetectionResult<Institution> {
        val duplicates = mutableListOf<DuplicatePair<Institution>>()
        val unique = mutableListOf<Institution>()
        
        newInstitutions.forEach { newInstitution ->
            val existing = existingInstitutions.find { 
                it.name.equals(newInstitution.name, ignoreCase = true) && 
                it.areaBrick.equals(newInstitution.areaBrick, ignoreCase = true)
            }
            if (existing != null) {
                duplicates.add(DuplicatePair(existing, newInstitution))
            } else {
                unique.add(newInstitution)
            }
        }
        
        return DuplicateDetectionResult(duplicates, unique)
    }
    
    /**
     * Detects duplicate wards based on institution and ward name
     */
    fun detectDuplicateWards(
        existingWards: List<Ward>,
        newWards: List<Ward>
    ): DuplicateDetectionResult<Ward> {
        val duplicates = mutableListOf<DuplicatePair<Ward>>()
        val unique = mutableListOf<Ward>()
        
        newWards.forEach { newWard ->
            val existing = existingWards.find { 
                it.institutionId == newWard.institutionId && 
                it.wardName.equals(newWard.wardName, ignoreCase = true)
            }
            if (existing != null) {
                duplicates.add(DuplicatePair(existing, newWard))
            } else {
                unique.add(newWard)
            }
        }
        
        return DuplicateDetectionResult(duplicates, unique)
    }
    
    /**
     * Detects duplicate assignments based on doctor, institution, and ward
     */
    fun detectDuplicateAssignments(
        existingAssignments: List<DoctorInstitution>,
        newAssignments: List<DoctorInstitution>
    ): DuplicateDetectionResult<DoctorInstitution> {
        val duplicates = mutableListOf<DuplicatePair<DoctorInstitution>>()
        val unique = mutableListOf<DoctorInstitution>()
        
        newAssignments.forEach { newAssignment ->
            val existing = existingAssignments.find { 
                it.doctorId == newAssignment.doctorId && 
                it.institutionId == newAssignment.institutionId &&
                it.wardId == newAssignment.wardId
            }
            if (existing != null) {
                duplicates.add(DuplicatePair(existing, newAssignment))
            } else {
                unique.add(newAssignment)
            }
        }
        
        return DuplicateDetectionResult(duplicates, unique)
    }
    
    /**
     * Detects duplicate saved filters based on name
     */
    fun detectDuplicateSavedFilters(
        existingFilters: List<SavedSearchFilter>,
        newFilters: List<SavedSearchFilter>
    ): DuplicateDetectionResult<SavedSearchFilter> {
        val duplicates = mutableListOf<DuplicatePair<SavedSearchFilter>>()
        val unique = mutableListOf<SavedSearchFilter>()
        
        newFilters.forEach { newFilter ->
            val existing = existingFilters.find { 
                it.name.equals(newFilter.name, ignoreCase = true)
            }
            if (existing != null) {
                duplicates.add(DuplicatePair(existing, newFilter))
            } else {
                unique.add(newFilter)
            }
        }
        
        return DuplicateDetectionResult(duplicates, unique)
    }
    
    // ========== MERGE STRATEGIES ==========
    
    /**
     * Merges doctors based on import strategy
     */
    fun mergeDoctors(
        duplicates: List<DuplicatePair<Doctor>>,
        strategy: ImportStrategy
    ): List<Doctor> {
        return when (strategy) {
            ImportStrategy.SKIP_DUPLICATES -> emptyList()
            ImportStrategy.UPDATE_DUPLICATES -> duplicates.map { mergeDoctorData(it.existing, it.new) }
            ImportStrategy.REPLACE_ALL -> duplicates.map { it.new }
        }
    }
    
    /**
     * Merges institutions based on import strategy
     */
    fun mergeInstitutions(
        duplicates: List<DuplicatePair<Institution>>,
        strategy: ImportStrategy
    ): List<Institution> {
        return when (strategy) {
            ImportStrategy.SKIP_DUPLICATES -> emptyList()
            ImportStrategy.UPDATE_DUPLICATES -> duplicates.map { mergeInstitutionData(it.existing, it.new) }
            ImportStrategy.REPLACE_ALL -> duplicates.map { it.new }
        }
    }
    
    /**
     * Merges wards based on import strategy
     */
    fun mergeWards(
        duplicates: List<DuplicatePair<Ward>>,
        strategy: ImportStrategy
    ): List<Ward> {
        return when (strategy) {
            ImportStrategy.SKIP_DUPLICATES -> emptyList()
            ImportStrategy.UPDATE_DUPLICATES -> duplicates.map { mergeWardData(it.existing, it.new) }
            ImportStrategy.REPLACE_ALL -> duplicates.map { it.new }
        }
    }
    
    /**
     * Merges assignments based on import strategy
     */
    fun mergeAssignments(
        duplicates: List<DuplicatePair<DoctorInstitution>>,
        strategy: ImportStrategy
    ): List<DoctorInstitution> {
        return when (strategy) {
            ImportStrategy.SKIP_DUPLICATES -> emptyList()
            ImportStrategy.UPDATE_DUPLICATES -> duplicates.map { mergeAssignmentData(it.existing, it.new) }
            ImportStrategy.REPLACE_ALL -> duplicates.map { it.new }
        }
    }
    
    /**
     * Merges saved filters based on import strategy
     */
    fun mergeSavedFilters(
        duplicates: List<DuplicatePair<SavedSearchFilter>>,
        strategy: ImportStrategy
    ): List<SavedSearchFilter> {
        return when (strategy) {
            ImportStrategy.SKIP_DUPLICATES -> emptyList()
            ImportStrategy.UPDATE_DUPLICATES -> duplicates.map { mergeSavedFilterData(it.existing, it.new) }
            ImportStrategy.REPLACE_ALL -> duplicates.map { it.new }
        }
    }
    
    // ========== DATA MERGING LOGIC ==========
    
    /**
     * Merges doctor data intelligently
     */
    private fun mergeDoctorData(existing: Doctor, new: Doctor): Doctor {
        return existing.copy(
            name = if (new.name.isNotBlank()) new.name else existing.name,
            speciality = if (new.speciality.isNotBlank()) new.speciality else existing.speciality,
            mobileNumber = if (new.mobileNumber.isNotBlank()) new.mobileNumber else existing.mobileNumber,
            qualifications = mergeQualifications(existing.qualifications, new.qualifications)
        )
    }
    
    /**
     * Merges institution data intelligently
     */
    private fun mergeInstitutionData(existing: Institution, new: Institution): Institution {
        return existing.copy(
            msName = if (new.msName.isNotBlank()) new.msName else existing.msName,
            dmsName = if (new.dmsName.isNotBlank()) new.dmsName else existing.dmsName,
            segmentName = if (new.segmentName.isNotBlank()) new.segmentName else existing.segmentName,
            numberOfWards = if (new.numberOfWards > 0) new.numberOfWards else existing.numberOfWards
        )
    }
    
    /**
     * Merges ward data intelligently
     */
    private fun mergeWardData(existing: Ward, new: Ward): Ward {
        return existing.copy(
            opdDays = if (new.opdDays.isNotEmpty()) new.opdDays else existing.opdDays,
            otDays = if (new.otDays.isNotEmpty()) new.otDays else existing.otDays
        )
    }
    
    /**
     * Merges assignment data intelligently
     */
    private fun mergeAssignmentData(existing: DoctorInstitution, new: DoctorInstitution): DoctorInstitution {
        return existing.copy(
            designation = if (new.designation.isNotBlank()) new.designation else existing.designation,
            dutyShift = new.dutyShift, // Always use new duty shift
            dutyDays = if (new.dutyDays.isNotEmpty()) new.dutyDays else existing.dutyDays
        )
    }
    
    /**
     * Merges saved filter data intelligently
     */
    private fun mergeSavedFilterData(existing: SavedSearchFilter, new: SavedSearchFilter): SavedSearchFilter {
        return existing.copy(
            description = if (new.description.isNotBlank()) new.description else existing.description,
            filterData = new.filterData, // Always use new filter data
            tags = mergeTags(existing.tags, new.tags),
            isFavorite = new.isFavorite || existing.isFavorite // Keep favorite status if either is favorite
        )
    }
    
    /**
     * Merges qualification lists, removing duplicates
     */
    private fun mergeQualifications(existing: List<String>, new: List<String>): List<String> {
        return (existing + new).distinct().filter { it.isNotBlank() }
    }
    
    /**
     * Merges tag lists, removing duplicates
     */
    private fun mergeTags(existing: List<String>, new: List<String>): List<String> {
        return (existing + new).distinct().filter { it.isNotBlank() }
    }
    
    // ========== VALIDATION ==========
    
    /**
     * Validates merged data integrity
     */
    fun validateMergedData(
        doctors: List<Doctor>,
        institutions: List<Institution>,
        wards: List<Ward>,
        assignments: List<DoctorInstitution>
    ): MergeValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Validate doctor data
        doctors.forEach { doctor ->
            if (!doctor.isValid()) {
                errors.add("Invalid doctor data: ${doctor.name}")
            }
        }
        
        // Validate institution data
        institutions.forEach { institution ->
            if (!institution.isValid()) {
                errors.add("Invalid institution data: ${institution.name}")
            }
        }
        
        // Validate ward data and references
        wards.forEach { ward ->
            if (!ward.isValid()) {
                errors.add("Invalid ward data: ${ward.wardName}")
            }
            if (institutions.none { it.institutionId == ward.institutionId }) {
                errors.add("Ward ${ward.wardName} references non-existent institution ID: ${ward.institutionId}")
            }
        }
        
        // Validate assignment data and references
        assignments.forEach { assignment ->
            if (!assignment.isValid()) {
                errors.add("Invalid assignment data: ID ${assignment.assignmentId}")
            }
            if (doctors.none { it.doctorId == assignment.doctorId }) {
                errors.add("Assignment references non-existent doctor ID: ${assignment.doctorId}")
            }
            if (institutions.none { it.institutionId == assignment.institutionId }) {
                errors.add("Assignment references non-existent institution ID: ${assignment.institutionId}")
            }
            assignment.wardId?.let { wardId ->
                if (wards.none { it.wardId == wardId }) {
                    warnings.add("Assignment references non-existent ward ID: $wardId")
                }
            }
        }
        
        return MergeValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}

/**
 * Result of duplicate detection
 */
data class DuplicateDetectionResult<T>(
    val duplicates: List<DuplicatePair<T>>,
    val unique: List<T>
) {
    val hasDuplicates: Boolean get() = duplicates.isNotEmpty()
    val duplicateCount: Int get() = duplicates.size
    val uniqueCount: Int get() = unique.size
    val totalCount: Int get() = duplicateCount + uniqueCount
}

/**
 * Pair of duplicate items (existing and new)
 */
data class DuplicatePair<T>(
    val existing: T,
    val new: T
)

/**
 * Result of merge validation
 */
data class MergeValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    fun getSummary(): String {
        return when {
            !isValid -> "Validation failed with ${errors.size} errors"
            warnings.isNotEmpty() -> "Validation passed with ${warnings.size} warnings"
            else -> "Validation passed successfully"
        }
    }
}