package com.healthtracker.offline.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward
import com.healthtracker.offline.data.dao.*
import com.healthtracker.offline.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced implementation of SearchRepository using Room database.
 * 
 * Provides comprehensive search capabilities with filter management,
 * performance optimization, and analytics.
 */
@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val doctorDao: DoctorDao,
    private val institutionDao: InstitutionDao,
    private val wardDao: WardDao,
    private val doctorInstitutionDao: DoctorInstitutionDao,
    private val savedSearchFilterDao: SavedSearchFilterDao
) : SearchRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // ========== GLOBAL SEARCH ==========
    
    override suspend fun globalSearch(filter: GlobalSearchFilter): Result<GlobalSearchResults> {
        return try {
            val startTime = System.currentTimeMillis()
            val searchId = generateSearchId()
            
            val doctors = if (filter.searchDoctors) {
                if (filter.query.isNotBlank()) {
                    doctorDao.searchDoctorsByName(filter.query).first()
                } else if (!filter.doctorFilter.isEmpty()) {
                    searchDoctors(filter.doctorFilter).first()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
            
            val institutions = if (filter.searchInstitutions) {
                if (filter.query.isNotBlank()) {
                    institutionDao.searchInstitutionsByName(filter.query).first()
                } else if (!filter.institutionFilter.isEmpty()) {
                    searchInstitutions(filter.institutionFilter).first()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
            
            val wards = if (filter.searchWards) {
                if (filter.query.isNotBlank()) {
                    wardDao.searchWardsByName(filter.query).first()
                } else if (!filter.wardFilter.isEmpty()) {
                    searchWards(filter.wardFilter).first()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
            
            val assignments = if (filter.searchAssignments) {
                if (filter.query.isNotBlank()) {
                    doctorInstitutionDao.searchAssignments(
                        doctorName = "%${filter.query}%",
                        speciality = "%",
                        institutionName = "%${filter.query}%",
                        areaBrick = "%",
                        dutyShift = null,
                        dutyDay = ""
                    ).first()
                } else if (!filter.assignmentFilter.isEmpty()) {
                    searchAssignments(filter.assignmentFilter).first()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            val totalResults = doctors.size + institutions.size + wards.size + assignments.size
            
            val metadata = SearchResultMetadata(
                totalResults = totalResults,
                executionTimeMs = executionTime,
                searchQuery = filter.query,
                appliedFilters = getAppliedFilters(filter),
                resultsByType = mapOf(
                    "doctors" to doctors.size,
                    "institutions" to institutions.size,
                    "wards" to wards.size,
                    "assignments" to assignments.size
                ),
                searchId = searchId
            )
            
            val results = GlobalSearchResults(
                doctors = doctors,
                institutions = institutions,
                wards = wards,
                assignments = assignments,
                metadata = metadata
            )
            
            // Record search for analytics
            recordSearchQuery(filter.query, totalResults, executionTime)
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSearchSuggestions(query: String, maxSuggestions: Int): List<SearchSuggestion> {
        return try {
            val suggestions = mutableListOf<SearchSuggestion>()
            val lowerQuery = query.lowercase()
            
            // Doctor name suggestions
            val doctorNames = doctorDao.searchDoctorsByName(query).first()
                .take(maxSuggestions / 4)
                .map { SearchSuggestion(it.name, SearchSuggestionType.DOCTOR_NAME, "Doctors") }
            suggestions.addAll(doctorNames)
            
            // Speciality suggestions
            val specialities = doctorDao.getAllSpecialities().first()
                .filter { it.lowercase().contains(lowerQuery) }
                .take(maxSuggestions / 4)
                .map { SearchSuggestion(it, SearchSuggestionType.SPECIALITY, "Specialities") }
            suggestions.addAll(specialities)
            
            // Institution name suggestions
            val institutionNames = institutionDao.searchInstitutionsByName(query).first()
                .take(maxSuggestions / 4)
                .map { SearchSuggestion(it.name, SearchSuggestionType.INSTITUTION_NAME, "Institutions") }
            suggestions.addAll(institutionNames)
            
            // Area brick suggestions
            val areaBricks = institutionDao.getAllAreaBricks().first()
                .filter { it.lowercase().contains(lowerQuery) }
                .take(maxSuggestions / 4)
                .map { SearchSuggestion(it, SearchSuggestionType.AREA_BRICK, "Areas") }
            suggestions.addAll(areaBricks)
            
            suggestions.take(maxSuggestions)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ========== ENTITY-SPECIFIC SEARCH ==========
    
    override fun searchDoctors(filter: DoctorSearchFilter): Flow<List<Doctor>> {
        val nameQuery = if (filter.nameQuery.isNotBlank()) "%${filter.nameQuery}%" else "%"
        val speciality = filter.speciality.ifBlank { "%" }
        
        return doctorDao.searchDoctors(nameQuery, speciality)
            .map { doctors ->
                doctors.filter { doctor ->
                    // Additional filtering that can't be done in SQL
                    (filter.pmdcNumber.isBlank() || doctor.pmdcNumber.contains(filter.pmdcNumber, ignoreCase = true)) &&
                    (filter.mobileNumber.isBlank() || doctor.mobileNumber.contains(filter.mobileNumber)) &&
                    (filter.qualifications.isEmpty() || filter.qualifications.any { qual ->
                        doctor.qualifications.any { it.contains(qual, ignoreCase = true) }
                    })
                }
            }
    }
    
    override fun searchInstitutions(filter: InstitutionSearchFilter): Flow<List<Institution>> {
        val nameQuery = if (filter.nameQuery.isNotBlank()) "%${filter.nameQuery}%" else "%"
        val areaBrick = filter.areaBrick.ifBlank { "%" }
        val segmentName = filter.segmentName.ifBlank { "%" }
        
        return institutionDao.searchInstitutions(nameQuery, areaBrick, segmentName)
            .map { institutions ->
                institutions.filter { institution ->
                    // Additional filtering
                    (filter.msName.isBlank() || institution.msName.contains(filter.msName, ignoreCase = true)) &&
                    (filter.dmsName.isBlank() || institution.dmsName.contains(filter.dmsName, ignoreCase = true)) &&
                    (filter.wardCountRange?.let { institution.numberOfWards in it } != false)
                }
            }
    }
    
    override fun searchWards(filter: WardSearchFilter): Flow<List<Ward>> {
        val institutionId = filter.institutionId ?: 0
        val opdDay = filter.hasOpdOnDay
        val otDay = filter.hasOtOnDay
        
        return wardDao.searchWards(institutionId, opdDay, otDay)
            .map { wards ->
                wards.filter { ward ->
                    // Additional filtering
                    (filter.nameQuery.isBlank() || ward.wardName.contains(filter.nameQuery, ignoreCase = true)) &&
                    (filter.opdDays.isEmpty() || filter.opdDays.any { day -> ward.opdDays.contains(day) }) &&
                    (filter.otDays.isEmpty() || filter.otDays.any { day -> ward.otDays.contains(day) })
                }
            }
    }
    
    override fun searchAssignments(filter: AssignmentSearchFilter): Flow<List<AssignmentWithDetails>> {
        val doctorName = if (filter.doctorName.isNotBlank()) "%${filter.doctorName}%" else "%"
        val speciality = filter.doctorSpeciality.ifBlank { "%" }
        val institutionName = if (filter.institutionName.isNotBlank()) "%${filter.institutionName}%" else "%"
        val areaBrick = filter.areaBrick.ifBlank { "%" }
        val dutyDay = filter.onDutyOnDay
        
        return doctorInstitutionDao.searchAssignments(
            doctorName = doctorName,
            speciality = speciality,
            institutionName = institutionName,
            areaBrick = areaBrick,
            dutyShift = filter.dutyShift,
            dutyDay = dutyDay
        ).map { assignments ->
            assignments.filter { assignment ->
                // Additional filtering
                (filter.wardName.isBlank() || assignment.wardName?.contains(filter.wardName, ignoreCase = true) == true) &&
                (filter.designation.isBlank() || assignment.designation.contains(filter.designation, ignoreCase = true)) &&
                (filter.dutyDays.isEmpty() || filter.dutyDays.any { day -> assignment.dutyDays.contains(day) })
            }
        }
    }
    
    // ========== SAVED FILTERS ==========
    
    override suspend fun saveSearchFilter(filter: SavedSearchFilter): Result<Long> {
        return try {
            val filterId = savedSearchFilterDao.insertSavedFilter(filter)
            Result.success(filterId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getSavedSearchFilters(): Flow<List<SavedSearchFilter>> {
        return savedSearchFilterDao.getAllSavedFilters()
    }
    
    override fun getSavedFiltersByType(filterType: SearchFilterType): Flow<List<SavedSearchFilter>> {
        return savedSearchFilterDao.getSavedFiltersByType(filterType)
    }
    
    override fun getFavoriteFilters(): Flow<List<SavedSearchFilter>> {
        return savedSearchFilterDao.getFavoriteSavedFilters()
    }
    
    override fun getRecentFilters(limit: Int): Flow<List<SavedSearchFilter>> {
        return savedSearchFilterDao.getRecentlyUsedFilters(limit)
    }
    
    override suspend fun updateSavedFilter(filter: SavedSearchFilter): Result<Unit> {
        return try {
            val rowsAffected = savedSearchFilterDao.updateSavedFilter(filter)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Filter not found or no changes made"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSavedFilter(filterId: Long): Result<Unit> {
        return try {
            val rowsAffected = savedSearchFilterDao.deleteSavedFilterById(filterId)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Filter not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun applySavedFilter(filterId: Long): Result<GlobalSearchResults> {
        return try {
            val savedFilter = savedSearchFilterDao.getSavedFilterById(filterId)
                ?: return Result.failure(Exception("Filter not found"))
            
            // Record usage
            recordFilterUsage(filterId)
            
            // Parse and apply the filter
            val globalFilter = when (savedFilter.filterType) {
                SearchFilterType.GLOBAL -> json.decodeFromString<GlobalSearchFilter>(savedFilter.filterData)
                SearchFilterType.DOCTOR -> {
                    val doctorFilter = json.decodeFromString<DoctorSearchFilter>(savedFilter.filterData)
                    GlobalSearchFilter(doctorFilter = doctorFilter, searchDoctors = true, searchInstitutions = false, searchWards = false, searchAssignments = false)
                }
                SearchFilterType.INSTITUTION -> {
                    val institutionFilter = json.decodeFromString<InstitutionSearchFilter>(savedFilter.filterData)
                    GlobalSearchFilter(institutionFilter = institutionFilter, searchDoctors = false, searchInstitutions = true, searchWards = false, searchAssignments = false)
                }
                SearchFilterType.WARD -> {
                    val wardFilter = json.decodeFromString<WardSearchFilter>(savedFilter.filterData)
                    GlobalSearchFilter(wardFilter = wardFilter, searchDoctors = false, searchInstitutions = false, searchWards = true, searchAssignments = false)
                }
                SearchFilterType.ASSIGNMENT -> {
                    val assignmentFilter = json.decodeFromString<AssignmentSearchFilter>(savedFilter.filterData)
                    GlobalSearchFilter(assignmentFilter = assignmentFilter, searchDoctors = false, searchInstitutions = false, searchWards = false, searchAssignments = true)
                }
            }
            
            globalSearch(globalFilter)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun recordFilterUsage(filterId: Long): Result<Unit> {
        return try {
            savedSearchFilterDao.incrementFilterUsage(filterId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== SEARCH ANALYTICS ==========
    
    override suspend fun getSearchMetrics(): SearchMetrics {
        return try {
            val totalFilters = savedSearchFilterDao.getSavedFilterCount()
            val recentFilters = savedSearchFilterDao.getRecentlyUsedFilters(10).first()
            val popularFilters = savedSearchFilterDao.getMostUsedFilters(10).first()
            
            SearchMetrics(
                totalSearches = totalFilters.toLong(),
                averageExecutionTime = 0.0, // Would be calculated from stored metrics
                mostSearchedTerms = popularFilters.map { it.name },
                slowestQueries = emptyList(), // Would be calculated from stored metrics
                searchesByType = SearchFilterType.values().associateWith { type ->
                    savedSearchFilterDao.getSavedFilterCountByType(type).toLong()
                }
            )
        } catch (e: Exception) {
            SearchMetrics()
        }
    }
    
    override suspend fun recordSearchQuery(query: String, resultCount: Int, executionTime: Long) {
        // Implementation would store search analytics in a separate table
        // For now, this is a placeholder
    }
    
    override suspend fun getPopularSearchTerms(limit: Int): List<Pair<String, Int>> {
        // Implementation would analyze stored search queries
        // For now, return empty list
        return emptyList()
    }
    
    // ========== FILTER VALIDATION ==========
    
    override suspend fun validateFilter(filter: SearchFilter): FilterValidationResult {
        return FilterValidator.validateFilter(filter)
    }
    
    override suspend fun optimizeFilter(filter: SearchFilter): SearchFilter {
        // Basic optimization - remove empty criteria
        return when (filter) {
            is DoctorSearchFilter -> filter.copy(
                nameQuery = filter.nameQuery.trim(),
                speciality = filter.speciality.trim(),
                pmdcNumber = filter.pmdcNumber.trim(),
                mobileNumber = filter.mobileNumber.trim()
            )
            is InstitutionSearchFilter -> filter.copy(
                nameQuery = filter.nameQuery.trim(),
                areaBrick = filter.areaBrick.trim(),
                segmentName = filter.segmentName.trim()
            )
            else -> filter
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private fun generateSearchId(): String {
        return "search_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun getAppliedFilters(filter: GlobalSearchFilter): List<String> {
        val filters = mutableListOf<String>()
        if (filter.query.isNotBlank()) filters.add("Global: ${filter.query}")
        if (!filter.doctorFilter.isEmpty()) filters.add("Doctor filters")
        if (!filter.institutionFilter.isEmpty()) filters.add("Institution filters")
        if (!filter.wardFilter.isEmpty()) filters.add("Ward filters")
        if (!filter.assignmentFilter.isEmpty()) filters.add("Assignment filters")
        return filters
    }
}