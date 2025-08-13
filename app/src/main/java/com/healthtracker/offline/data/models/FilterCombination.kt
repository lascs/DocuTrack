package com.healthtracker.offline.data.models

import kotlinx.serialization.Serializable

/**
 * Represents how multiple filters should be combined
 */
enum class FilterCombinationType {
    AND,    // All filters must match
    OR,     // Any filter can match
    NOT     // Exclude results that match
}

/**
 * A combination of multiple search filters with logical operators
 */
@Serializable
data class FilterCombination(
    val filters: List<CombinedFilter> = emptyList(),
    val globalOperator: FilterCombinationType = FilterCombinationType.AND
) {
    
    /**
     * Checks if the combination is empty
     */
    fun isEmpty(): Boolean = filters.isEmpty() || filters.all { it.filter.isEmpty() }
    
    /**
     * Adds a filter to the combination
     */
    fun addFilter(filter: SearchFilter, operator: FilterCombinationType = FilterCombinationType.AND): FilterCombination {
        val combinedFilter = CombinedFilter(filter, operator)
        return copy(filters = filters + combinedFilter)
    }
    
    /**
     * Removes a filter from the combination
     */
    fun removeFilter(index: Int): FilterCombination {
        return if (index in filters.indices) {
            copy(filters = filters.toMutableList().apply { removeAt(index) })
        } else {
            this
        }
    }
    
    /**
     * Gets a human-readable description of the filter combination
     */
    fun getDescription(): String {
        if (filters.isEmpty()) return "No filters applied"
        
        val descriptions = filters.map { combinedFilter ->
            val filterDesc = when (combinedFilter.filter) {
                is DoctorSearchFilter -> "Doctor: ${combinedFilter.filter.toQueryString()}"
                is InstitutionSearchFilter -> "Institution: ${combinedFilter.filter.toQueryString()}"
                is WardSearchFilter -> "Ward: ${combinedFilter.filter.toQueryString()}"
                is AssignmentSearchFilter -> "Assignment: ${combinedFilter.filter.toQueryString()}"
                is GlobalSearchFilter -> "Global: ${combinedFilter.filter.toQueryString()}"
                else -> "Unknown filter"
            }
            
            when (combinedFilter.operator) {
                FilterCombinationType.NOT -> "NOT ($filterDesc)"
                else -> filterDesc
            }
        }
        
        return descriptions.joinToString(" ${globalOperator.name} ")
    }
    
    companion object {
        fun empty() = FilterCombination()
        
        fun single(filter: SearchFilter) = FilterCombination(
            filters = listOf(CombinedFilter(filter, FilterCombinationType.AND))
        )
    }
}

/**
 * A filter with its combination operator
 */
@Serializable
data class CombinedFilter(
    val filter: SearchFilter,
    val operator: FilterCombinationType = FilterCombinationType.AND
)

/**
 * Validation result for search filters
 */
data class FilterValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun valid() = FilterValidationResult(true)
        fun invalid(errors: List<String>) = FilterValidationResult(false, errors)
        fun withWarnings(warnings: List<String>) = FilterValidationResult(true, warnings = warnings)
    }
}

/**
 * Utility class for validating search filters
 */
object FilterValidator {
    
    /**
     * Validates a doctor search filter
     */
    fun validateDoctorFilter(filter: DoctorSearchFilter): FilterValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check for potentially problematic patterns
        if (filter.nameQuery.length == 1) {
            warnings.add("Single character name search may return too many results")
        }
        
        if (filter.pmdcNumber.isNotBlank() && filter.pmdcNumber.length < 3) {
            warnings.add("Short PMDC number search may not be specific enough")
        }
        
        if (filter.mobileNumber.isNotBlank() && !filter.mobileNumber.matches(Regex("^[0-9+\\-\\s()]*$"))) {
            errors.add("Mobile number contains invalid characters")
        }
        
        filter.assignmentCount?.let { range ->
            if (range.first < 0) {
                errors.add("Assignment count cannot be negative")
            }
            if (range.first > range.last) {
                errors.add("Assignment count range is invalid")
            }
        }
        
        return when {
            errors.isNotEmpty() -> FilterValidationResult.invalid(errors)
            warnings.isNotEmpty() -> FilterValidationResult.withWarnings(warnings)
            else -> FilterValidationResult.valid()
        }
    }
    
    /**
     * Validates an institution search filter
     */
    fun validateInstitutionFilter(filter: InstitutionSearchFilter): FilterValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        if (filter.nameQuery.length == 1) {
            warnings.add("Single character name search may return too many results")
        }
        
        filter.wardCountRange?.let { range ->
            if (range.first < 0) {
                errors.add("Ward count cannot be negative")
            }
            if (range.first > range.last) {
                errors.add("Ward count range is invalid")
            }
            if (range.last > 1000) {
                warnings.add("Very high ward count filter may not match any institutions")
            }
        }
        
        return when {
            errors.isNotEmpty() -> FilterValidationResult.invalid(errors)
            warnings.isNotEmpty() -> FilterValidationResult.withWarnings(warnings)
            else -> FilterValidationResult.valid()
        }
    }
    
    /**
     * Validates a ward search filter
     */
    fun validateWardFilter(filter: WardSearchFilter): FilterValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        if (filter.nameQuery.length == 1) {
            warnings.add("Single character name search may return too many results")
        }
        
        // Validate day names
        val validDays = setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        filter.opdDays.forEach { day ->
            if (day !in validDays) {
                errors.add("Invalid OPD day: $day")
            }
        }
        
        filter.otDays.forEach { day ->
            if (day !in validDays) {
                errors.add("Invalid OT day: $day")
            }
        }
        
        if (filter.hasOpdOnDay.isNotBlank() && filter.hasOpdOnDay !in validDays) {
            errors.add("Invalid OPD day: ${filter.hasOpdOnDay}")
        }
        
        if (filter.hasOtOnDay.isNotBlank() && filter.hasOtOnDay !in validDays) {
            errors.add("Invalid OT day: ${filter.hasOtOnDay}")
        }
        
        return when {
            errors.isNotEmpty() -> FilterValidationResult.invalid(errors)
            warnings.isNotEmpty() -> FilterValidationResult.withWarnings(warnings)
            else -> FilterValidationResult.valid()
        }
    }
    
    /**
     * Validates an assignment search filter
     */
    fun validateAssignmentFilter(filter: AssignmentSearchFilter): FilterValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        if (filter.doctorName.length == 1 || filter.institutionName.length == 1) {
            warnings.add("Single character name search may return too many results")
        }
        
        // Validate duty days
        val validDays = setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        filter.dutyDays.forEach { day ->
            if (day !in validDays) {
                errors.add("Invalid duty day: $day")
            }
        }
        
        if (filter.onDutyOnDay.isNotBlank() && filter.onDutyOnDay !in validDays) {
            errors.add("Invalid duty day: ${filter.onDutyOnDay}")
        }
        
        return when {
            errors.isNotEmpty() -> FilterValidationResult.invalid(errors)
            warnings.isNotEmpty() -> FilterValidationResult.withWarnings(warnings)
            else -> FilterValidationResult.valid()
        }
    }
    
    /**
     * Validates a global search filter
     */
    fun validateGlobalFilter(filter: GlobalSearchFilter): FilterValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        if (filter.query.length == 1) {
            warnings.add("Single character global search may return too many results")
        }
        
        if (filter.query.length > 100) {
            warnings.add("Very long search query may not perform well")
        }
        
        // Validate sub-filters
        if (!filter.doctorFilter.isEmpty()) {
            val doctorValidation = validateDoctorFilter(filter.doctorFilter)
            errors.addAll(doctorValidation.errors)
            warnings.addAll(doctorValidation.warnings)
        }
        
        if (!filter.institutionFilter.isEmpty()) {
            val institutionValidation = validateInstitutionFilter(filter.institutionFilter)
            errors.addAll(institutionValidation.errors)
            warnings.addAll(institutionValidation.warnings)
        }
        
        if (!filter.wardFilter.isEmpty()) {
            val wardValidation = validateWardFilter(filter.wardFilter)
            errors.addAll(wardValidation.errors)
            warnings.addAll(wardValidation.warnings)
        }
        
        if (!filter.assignmentFilter.isEmpty()) {
            val assignmentValidation = validateAssignmentFilter(filter.assignmentFilter)
            errors.addAll(assignmentValidation.errors)
            warnings.addAll(assignmentValidation.warnings)
        }
        
        return when {
            errors.isNotEmpty() -> FilterValidationResult.invalid(errors)
            warnings.isNotEmpty() -> FilterValidationResult.withWarnings(warnings)
            else -> FilterValidationResult.valid()
        }
    }
    
    /**
     * Validates any search filter
     */
    fun validateFilter(filter: SearchFilter): FilterValidationResult {
        return when (filter) {
            is DoctorSearchFilter -> validateDoctorFilter(filter)
            is InstitutionSearchFilter -> validateInstitutionFilter(filter)
            is WardSearchFilter -> validateWardFilter(filter)
            is AssignmentSearchFilter -> validateAssignmentFilter(filter)
            is GlobalSearchFilter -> validateGlobalFilter(filter)
            else -> FilterValidationResult.invalid(listOf("Unknown filter type"))
        }
    }
}