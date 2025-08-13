package com.healthtracker.offline.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.healthtracker.offline.data.converters.StringListConverter
import com.healthtracker.offline.data.entities.DutyShift
import kotlinx.serialization.Serializable

/**
 * Base interface for all search filters
 */
interface SearchFilter {
    fun isEmpty(): Boolean
    fun toQueryString(): String
}

/**
 * Comprehensive search filter for doctors with all possible criteria
 */
@Serializable
data class DoctorSearchFilter(
    val nameQuery: String = "",
    val speciality: String = "",
    val pmdcNumber: String = "",
    val mobileNumber: String = "",
    val qualifications: List<String> = emptyList(),
    val hasAssignments: Boolean? = null,
    val assignmentCount: IntRange? = null
) : SearchFilter {
    
    override fun isEmpty(): Boolean {
        return nameQuery.isBlank() && 
               speciality.isBlank() && 
               pmdcNumber.isBlank() && 
               mobileNumber.isBlank() && 
               qualifications.isEmpty() && 
               hasAssignments == null && 
               assignmentCount == null
    }
    
    override fun toQueryString(): String {
        val parts = mutableListOf<String>()
        if (nameQuery.isNotBlank()) parts.add("name:$nameQuery")
        if (speciality.isNotBlank()) parts.add("speciality:$speciality")
        if (pmdcNumber.isNotBlank()) parts.add("pmdc:$pmdcNumber")
        if (mobileNumber.isNotBlank()) parts.add("mobile:$mobileNumber")
        if (qualifications.isNotEmpty()) parts.add("qualifications:${qualifications.joinToString(",")}")
        hasAssignments?.let { parts.add("hasAssignments:$it") }
        assignmentCount?.let { parts.add("assignmentCount:${it.first}-${it.last}") }
        return parts.joinToString(" ")
    }
    
    companion object {
        fun empty() = DoctorSearchFilter()
    }
}

/**
 * Comprehensive search filter for institutions with all possible criteria
 */
@Serializable
data class InstitutionSearchFilter(
    val nameQuery: String = "",
    val areaBrick: String = "",
    val segmentName: String = "",
    val msName: String = "",
    val dmsName: String = "",
    val wardCountRange: IntRange? = null,
    val hasWards: Boolean? = null,
    val hasDoctors: Boolean? = null
) : SearchFilter {
    
    override fun isEmpty(): Boolean {
        return nameQuery.isBlank() && 
               areaBrick.isBlank() && 
               segmentName.isBlank() && 
               msName.isBlank() && 
               dmsName.isBlank() && 
               wardCountRange == null && 
               hasWards == null && 
               hasDoctors == null
    }
    
    override fun toQueryString(): String {
        val parts = mutableListOf<String>()
        if (nameQuery.isNotBlank()) parts.add("name:$nameQuery")
        if (areaBrick.isNotBlank()) parts.add("area:$areaBrick")
        if (segmentName.isNotBlank()) parts.add("segment:$segmentName")
        if (msName.isNotBlank()) parts.add("ms:$msName")
        if (dmsName.isNotBlank()) parts.add("dms:$dmsName")
        wardCountRange?.let { parts.add("wards:${it.first}-${it.last}") }
        hasWards?.let { parts.add("hasWards:$it") }
        hasDoctors?.let { parts.add("hasDoctors:$it") }
        return parts.joinToString(" ")
    }
    
    companion object {
        fun empty() = InstitutionSearchFilter()
    }
}

/**
 * Comprehensive search filter for wards with all possible criteria
 */
@Serializable
data class WardSearchFilter(
    val nameQuery: String = "",
    val institutionId: Int? = null,
    val institutionName: String = "",
    val opdDays: List<String> = emptyList(),
    val otDays: List<String> = emptyList(),
    val hasOpdOnDay: String = "",
    val hasOtOnDay: String = "",
    val hasDoctors: Boolean? = null
) : SearchFilter {
    
    override fun isEmpty(): Boolean {
        return nameQuery.isBlank() && 
               institutionId == null && 
               institutionName.isBlank() && 
               opdDays.isEmpty() && 
               otDays.isEmpty() && 
               hasOpdOnDay.isBlank() && 
               hasOtOnDay.isBlank() && 
               hasDoctors == null
    }
    
    override fun toQueryString(): String {
        val parts = mutableListOf<String>()
        if (nameQuery.isNotBlank()) parts.add("name:$nameQuery")
        institutionId?.let { parts.add("institutionId:$it") }
        if (institutionName.isNotBlank()) parts.add("institution:$institutionName")
        if (opdDays.isNotEmpty()) parts.add("opd:${opdDays.joinToString(",")}")
        if (otDays.isNotEmpty()) parts.add("ot:${otDays.joinToString(",")}")
        if (hasOpdOnDay.isNotBlank()) parts.add("opdDay:$hasOpdOnDay")
        if (hasOtOnDay.isNotBlank()) parts.add("otDay:$hasOtOnDay")
        hasDoctors?.let { parts.add("hasDoctors:$it") }
        return parts.joinToString(" ")
    }
    
    companion object {
        fun empty() = WardSearchFilter()
    }
}

/**
 * Comprehensive search filter for doctor-institution assignments
 */
@Serializable
data class AssignmentSearchFilter(
    val doctorName: String = "",
    val doctorSpeciality: String = "",
    val institutionName: String = "",
    val areaBrick: String = "",
    val wardName: String = "",
    val designation: String = "",
    val dutyShift: DutyShift? = null,
    val dutyDays: List<String> = emptyList(),
    val onDutyOnDay: String = ""
) : SearchFilter {
    
    override fun isEmpty(): Boolean {
        return doctorName.isBlank() && 
               doctorSpeciality.isBlank() && 
               institutionName.isBlank() && 
               areaBrick.isBlank() && 
               wardName.isBlank() && 
               designation.isBlank() && 
               dutyShift == null && 
               dutyDays.isEmpty() && 
               onDutyOnDay.isBlank()
    }
    
    override fun toQueryString(): String {
        val parts = mutableListOf<String>()
        if (doctorName.isNotBlank()) parts.add("doctor:$doctorName")
        if (doctorSpeciality.isNotBlank()) parts.add("speciality:$doctorSpeciality")
        if (institutionName.isNotBlank()) parts.add("institution:$institutionName")
        if (areaBrick.isNotBlank()) parts.add("area:$areaBrick")
        if (wardName.isNotBlank()) parts.add("ward:$wardName")
        if (designation.isNotBlank()) parts.add("designation:$designation")
        dutyShift?.let { parts.add("shift:${it.displayName}") }
        if (dutyDays.isNotEmpty()) parts.add("dutyDays:${dutyDays.joinToString(",")}")
        if (onDutyOnDay.isNotBlank()) parts.add("onDuty:$onDutyOnDay")
        return parts.joinToString(" ")
    }
    
    companion object {
        fun empty() = AssignmentSearchFilter()
    }
}

/**
 * Combined search filter that can search across all entity types
 */
@Serializable
data class GlobalSearchFilter(
    val query: String = "",
    val searchDoctors: Boolean = true,
    val searchInstitutions: Boolean = true,
    val searchWards: Boolean = true,
    val searchAssignments: Boolean = true,
    val doctorFilter: DoctorSearchFilter = DoctorSearchFilter.empty(),
    val institutionFilter: InstitutionSearchFilter = InstitutionSearchFilter.empty(),
    val wardFilter: WardSearchFilter = WardSearchFilter.empty(),
    val assignmentFilter: AssignmentSearchFilter = AssignmentSearchFilter.empty()
) : SearchFilter {
    
    override fun isEmpty(): Boolean {
        return query.isBlank() && 
               doctorFilter.isEmpty() && 
               institutionFilter.isEmpty() && 
               wardFilter.isEmpty() && 
               assignmentFilter.isEmpty()
    }
    
    override fun toQueryString(): String {
        val parts = mutableListOf<String>()
        if (query.isNotBlank()) parts.add("global:$query")
        if (!doctorFilter.isEmpty()) parts.add("doctors:[${doctorFilter.toQueryString()}]")
        if (!institutionFilter.isEmpty()) parts.add("institutions:[${institutionFilter.toQueryString()}]")
        if (!wardFilter.isEmpty()) parts.add("wards:[${wardFilter.toQueryString()}]")
        if (!assignmentFilter.isEmpty()) parts.add("assignments:[${assignmentFilter.toQueryString()}]")
        return parts.joinToString(" ")
    }
    
    companion object {
        fun empty() = GlobalSearchFilter()
        
        fun fromQuery(query: String) = GlobalSearchFilter(query = query)
    }
}

/**
 * Room entity for saving search filters
 */
@Entity(tableName = "saved_search_filters")
@TypeConverters(StringListConverter::class, com.healthtracker.offline.data.converters.SearchFilterTypeConverter::class)
data class SavedSearchFilter(
    @PrimaryKey(autoGenerate = true)
    val filterId: Long = 0,
    
    val name: String,
    
    val description: String = "",
    
    val filterType: SearchFilterType,
    
    val filterData: String, // JSON representation of the filter
    
    val tags: List<String> = emptyList(),
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val lastUsed: Long = 0,
    
    val useCount: Int = 0,
    
    val isFavorite: Boolean = false
) {
    /**
     * Checks if this filter matches the search query
     */
    fun matchesSearch(searchQuery: String): Boolean {
        val query = searchQuery.lowercase()
        return name.lowercase().contains(query) || 
               description.lowercase().contains(query) ||
               tags.any { it.lowercase().contains(query) }
    }
    
    /**
     * Returns a summary of the filter for display
     */
    fun getSummary(): String {
        return if (description.isNotBlank()) {
            description
        } else {
            "Filter for ${filterType.displayName}"
        }
    }
}

/**
 * Enum for different types of search filters
 */
enum class SearchFilterType(val displayName: String) {
    DOCTOR("Doctors"),
    INSTITUTION("Institutions"),
    WARD("Wards"),
    ASSIGNMENT("Assignments"),
    GLOBAL("Global Search");
    
    companion object {
        fun fromDisplayName(displayName: String): SearchFilterType? {
            return values().find { it.displayName == displayName }
        }
    }
}

/**
 * Data class for search suggestions
 */
@Serializable
data class SearchSuggestion(
    val text: String,
    val type: SearchSuggestionType,
    val category: String = "",
    val count: Int = 0,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getDisplayText(): String {
        return if (count > 0) "$text ($count)" else text
    }
}

/**
 * Enum for search suggestion types
 */
enum class SearchSuggestionType {
    DOCTOR_NAME,
    SPECIALITY,
    INSTITUTION_NAME,
    AREA_BRICK,
    WARD_NAME,
    DESIGNATION,
    RECENT_SEARCH,
    SAVED_FILTER
}

/**
 * Data class for search result metadata
 */
@Serializable
data class SearchResultMetadata(
    val totalResults: Int = 0,
    val executionTimeMs: Long = 0,
    val searchQuery: String = "",
    val appliedFilters: List<String> = emptyList(),
    val resultsByType: Map<String, Int> = emptyMap(),
    val hasMoreResults: Boolean = false,
    val searchId: String = ""
) {
    fun getFormattedExecutionTime(): String {
        return when {
            executionTimeMs < 1000 -> "${executionTimeMs}ms"
            executionTimeMs < 60000 -> "${executionTimeMs / 1000}s"
            else -> "${executionTimeMs / 60000}m ${(executionTimeMs % 60000) / 1000}s"
        }
    }
}