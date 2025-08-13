package com.healthtracker.offline.data.repository

import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward
import com.healthtracker.offline.data.dao.AssignmentWithDetails
import com.healthtracker.offline.data.models.*

/**
 * Repository interface for advanced search and filtering operations.
 * 
 * Provides comprehensive search capabilities with filter management,
 * suggestions, and performance optimization.
 */
interface SearchRepository {
    
    // ========== GLOBAL SEARCH ==========
    
    /**
     * Performs a global search across all entities
     * @param filter Global search filter with query and entity-specific filters
     * @return Result containing comprehensive search results
     */
    suspend fun globalSearch(filter: GlobalSearchFilter): Result<GlobalSearchResults>
    
    /**
     * Gets search suggestions based on partial input
     * @param query Partial search term
     * @param maxSuggestions Maximum number of suggestions to return
     * @return List of search suggestions with metadata
     */
    suspend fun getSearchSuggestions(query: String, maxSuggestions: Int = 10): List<SearchSuggestion>
    
    // ========== ENTITY-SPECIFIC SEARCH ==========
    
    /**
     * Advanced doctor search with comprehensive filtering
     * @param filter Doctor search filter with all criteria
     * @return Flow of matching doctors
     */
    fun searchDoctors(filter: DoctorSearchFilter): Flow<List<Doctor>>
    
    /**
     * Advanced institution search with comprehensive filtering
     * @param filter Institution search filter with all criteria
     * @return Flow of matching institutions
     */
    fun searchInstitutions(filter: InstitutionSearchFilter): Flow<List<Institution>>
    
    /**
     * Advanced ward search with comprehensive filtering
     * @param filter Ward search filter with all criteria
     * @return Flow of matching wards
     */
    fun searchWards(filter: WardSearchFilter): Flow<List<Ward>>
    
    /**
     * Advanced assignment search with comprehensive filtering
     * @param filter Assignment search filter with all criteria
     * @return Flow of matching assignments with details
     */
    fun searchAssignments(filter: AssignmentSearchFilter): Flow<List<AssignmentWithDetails>>
    
    // ========== SAVED FILTERS ==========
    
    /**
     * Saves a search filter for future use
     * @param filter Search filter to save with metadata
     * @return Result containing the saved filter ID
     */
    suspend fun saveSearchFilter(filter: SavedSearchFilter): Result<Long>
    
    /**
     * Gets all saved search filters
     * @return Flow of saved search filters ordered by usage
     */
    fun getSavedSearchFilters(): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets saved search filters by type
     * @param filterType Type of filters to retrieve
     * @return Flow of filters of the specified type
     */
    fun getSavedFiltersByType(filterType: SearchFilterType): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets favorite saved search filters
     * @return Flow of favorite filters
     */
    fun getFavoriteFilters(): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets recently used search filters
     * @param limit Maximum number of filters to return
     * @return Flow of recently used filters
     */
    fun getRecentFilters(limit: Int = 10): Flow<List<SavedSearchFilter>>
    
    /**
     * Updates a saved search filter
     * @param filter Updated filter data
     * @return Result indicating success or error
     */
    suspend fun updateSavedFilter(filter: SavedSearchFilter): Result<Unit>
    
    /**
     * Deletes a saved search filter
     * @param filterId Filter ID to delete
     * @return Result indicating success or error
     */
    suspend fun deleteSavedFilter(filterId: Long): Result<Unit>
    
    /**
     * Applies a saved search filter
     * @param filterId Filter ID to apply
     * @return Result containing search results
     */
    suspend fun applySavedFilter(filterId: Long): Result<GlobalSearchResults>
    
    /**
     * Records filter usage for analytics
     * @param filterId Filter ID that was used
     * @return Result indicating success or error
     */
    suspend fun recordFilterUsage(filterId: Long): Result<Unit>
    
    // ========== SEARCH ANALYTICS ==========
    
    /**
     * Gets search performance metrics
     * @return Search performance statistics
     */
    suspend fun getSearchMetrics(): SearchMetrics
    
    /**
     * Records search query for analytics and suggestions
     * @param query Search query
     * @param resultCount Number of results returned
     * @param executionTime Time taken to execute search in milliseconds
     */
    suspend fun recordSearchQuery(query: String, resultCount: Int, executionTime: Long)
    
    /**
     * Gets popular search terms
     * @param limit Maximum number of terms to return
     * @return List of popular search terms with usage counts
     */
    suspend fun getPopularSearchTerms(limit: Int = 20): List<Pair<String, Int>>
    
    // ========== FILTER VALIDATION ==========
    
    /**
     * Validates a search filter before execution
     * @param filter Filter to validate
     * @return Validation result with errors and warnings
     */
    suspend fun validateFilter(filter: SearchFilter): FilterValidationResult
    
    /**
     * Optimizes a search filter for better performance
     * @param filter Filter to optimize
     * @return Optimized filter with suggestions
     */
    suspend fun optimizeFilter(filter: SearchFilter): SearchFilter
}

/**
 * Enhanced global search results with metadata
 */
data class GlobalSearchResults(
    val doctors: List<Doctor> = emptyList(),
    val institutions: List<Institution> = emptyList(),
    val wards: List<Ward> = emptyList(),
    val assignments: List<AssignmentWithDetails> = emptyList(),
    val metadata: SearchResultMetadata = SearchResultMetadata()
) {
    val totalResults: Int
        get() = doctors.size + institutions.size + wards.size + assignments.size
}

/**
 * Search performance metrics
 */
data class SearchMetrics(
    val totalSearches: Long = 0,
    val averageExecutionTime: Double = 0.0,
    val mostSearchedTerms: List<String> = emptyList(),
    val slowestQueries: List<String> = emptyList(),
    val searchesByType: Map<SearchFilterType, Long> = emptyMap(),
    val peakSearchTimes: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)