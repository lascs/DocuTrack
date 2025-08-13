package com.healthtracker.offline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.healthtracker.offline.data.repository.SearchRepository
import com.healthtracker.offline.data.models.*
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward
import com.healthtracker.offline.data.dao.AssignmentWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing search and filtering operations.
 * 
 * Handles global search, advanced filtering, saved filters, and search suggestions.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {
    
    // ========== UI STATE ==========
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()
    
    private val _activeFilter = MutableStateFlow(GlobalSearchFilter.empty())
    val activeFilter: StateFlow<GlobalSearchFilter> = _activeFilter.asStateFlow()
    
    private val _searchResults = MutableStateFlow<GlobalSearchResults?>(null)
    val searchResults: StateFlow<GlobalSearchResults?> = _searchResults.asStateFlow()
    
    private val _searchSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val searchSuggestions: StateFlow<List<SearchSuggestion>> = _searchSuggestions.asStateFlow()
    
    // ========== SAVED FILTERS ==========
    
    val savedFilters: StateFlow<List<SavedSearchFilter>> = searchRepository.getSavedSearchFilters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val favoriteFilters: StateFlow<List<SavedSearchFilter>> = searchRepository.getFavoriteFilters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val recentFilters: StateFlow<List<SavedSearchFilter>> = searchRepository.getRecentFilters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== SEARCH ACTIONS ==========
    
    /**
     * Updates the global search query
     */
    fun updateGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
        
        // Update the active filter with the new query
        _activeFilter.value = _activeFilter.value.copy(query = query)
        
        // Get search suggestions if query is not empty
        if (query.isNotBlank() && query.length >= 2) {
            getSuggestions(query)
        } else {
            _searchSuggestions.value = emptyList()
        }
    }
    
    /**
     * Performs a global search with the current filter
     */
    fun performGlobalSearch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            searchRepository.globalSearch(_activeFilter.value)
                .onSuccess { results ->
                    _searchResults.value = results
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastSearchTime = System.currentTimeMillis()
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Search failed"
                    )
                }
        }
    }
    
    /**
     * Clears search results and filters
     */
    fun clearSearch() {
        _globalSearchQuery.value = ""
        _activeFilter.value = GlobalSearchFilter.empty()
        _searchResults.value = null
        _searchSuggestions.value = emptyList()
    }
    
    /**
     * Updates the active search filter
     */
    fun updateFilter(filter: GlobalSearchFilter) {
        _activeFilter.value = filter
        _globalSearchQuery.value = filter.query
    }
    
    /**
     * Updates doctor-specific filters
     */
    fun updateDoctorFilter(doctorFilter: DoctorSearchFilter) {
        _activeFilter.value = _activeFilter.value.copy(
            doctorFilter = doctorFilter,
            searchDoctors = !doctorFilter.isEmpty()
        )
    }
    
    /**
     * Updates institution-specific filters
     */
    fun updateInstitutionFilter(institutionFilter: InstitutionSearchFilter) {
        _activeFilter.value = _activeFilter.value.copy(
            institutionFilter = institutionFilter,
            searchInstitutions = !institutionFilter.isEmpty()
        )
    }
    
    /**
     * Updates ward-specific filters
     */
    fun updateWardFilter(wardFilter: WardSearchFilter) {
        _activeFilter.value = _activeFilter.value.copy(
            wardFilter = wardFilter,
            searchWards = !wardFilter.isEmpty()
        )
    }
    
    /**
     * Updates assignment-specific filters
     */
    fun updateAssignmentFilter(assignmentFilter: AssignmentSearchFilter) {
        _activeFilter.value = _activeFilter.value.copy(
            assignmentFilter = assignmentFilter,
            searchAssignments = !assignmentFilter.isEmpty()
        )
    }
    
    /**
     * Toggles which entity types to search
     */
    fun toggleSearchEntity(entityType: SearchEntityType, enabled: Boolean) {
        _activeFilter.value = when (entityType) {
            SearchEntityType.DOCTORS -> _activeFilter.value.copy(searchDoctors = enabled)
            SearchEntityType.INSTITUTIONS -> _activeFilter.value.copy(searchInstitutions = enabled)
            SearchEntityType.WARDS -> _activeFilter.value.copy(searchWards = enabled)
            SearchEntityType.ASSIGNMENTS -> _activeFilter.value.copy(searchAssignments = enabled)
        }
    }
    
    // ========== SAVED FILTER ACTIONS ==========
    
    /**
     * Saves the current filter
     */
    fun saveCurrentFilter(name: String, description: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val filterData = when {
                !_activeFilter.value.doctorFilter.isEmpty() -> {
                    SavedSearchFilter(
                        name = name,
                        description = description,
                        filterType = SearchFilterType.DOCTOR,
                        filterData = kotlinx.serialization.json.Json.encodeToString(_activeFilter.value.doctorFilter)
                    )
                }
                !_activeFilter.value.institutionFilter.isEmpty() -> {
                    SavedSearchFilter(
                        name = name,
                        description = description,
                        filterType = SearchFilterType.INSTITUTION,
                        filterData = kotlinx.serialization.json.Json.encodeToString(_activeFilter.value.institutionFilter)
                    )
                }
                !_activeFilter.value.wardFilter.isEmpty() -> {
                    SavedSearchFilter(
                        name = name,
                        description = description,
                        filterType = SearchFilterType.WARD,
                        filterData = kotlinx.serialization.json.Json.encodeToString(_activeFilter.value.wardFilter)
                    )
                }
                !_activeFilter.value.assignmentFilter.isEmpty() -> {
                    SavedSearchFilter(
                        name = name,
                        description = description,
                        filterType = SearchFilterType.ASSIGNMENT,
                        filterData = kotlinx.serialization.json.Json.encodeToString(_activeFilter.value.assignmentFilter)
                    )
                }
                else -> {
                    SavedSearchFilter(
                        name = name,
                        description = description,
                        filterType = SearchFilterType.GLOBAL,
                        filterData = kotlinx.serialization.json.Json.encodeToString(_activeFilter.value)
                    )
                }
            }
            
            searchRepository.saveSearchFilter(filterData)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Filter saved successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to save filter"
                    )
                }
        }
    }
    
    /**
     * Applies a saved filter
     */
    fun applySavedFilter(filterId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            searchRepository.applySavedFilter(filterId)
                .onSuccess { results ->
                    _searchResults.value = results
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastSearchTime = System.currentTimeMillis()
                    )
                    
                    // Record filter usage
                    searchRepository.recordFilterUsage(filterId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to apply filter"
                    )
                }
        }
    }
    
    /**
     * Deletes a saved filter
     */
    fun deleteSavedFilter(filterId: Long) {
        viewModelScope.launch {
            searchRepository.deleteSavedFilter(filterId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Filter deleted successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete filter"
                    )
                }
        }
    }
    
    // ========== SUGGESTIONS ==========
    
    /**
     * Gets search suggestions for the given query
     */
    private fun getSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val suggestions = searchRepository.getSearchSuggestions(query, 10)
                _searchSuggestions.value = suggestions
            } catch (e: Exception) {
                // Ignore suggestion errors
                _searchSuggestions.value = emptyList()
            }
        }
    }
    
    /**
     * Applies a search suggestion
     */
    fun applySuggestion(suggestion: SearchSuggestion) {
        when (suggestion.type) {
            SearchSuggestionType.DOCTOR_NAME -> {
                updateDoctorFilter(_activeFilter.value.doctorFilter.copy(nameQuery = suggestion.text))
            }
            SearchSuggestionType.SPECIALITY -> {
                updateDoctorFilter(_activeFilter.value.doctorFilter.copy(speciality = suggestion.text))
            }
            SearchSuggestionType.INSTITUTION_NAME -> {
                updateInstitutionFilter(_activeFilter.value.institutionFilter.copy(nameQuery = suggestion.text))
            }
            SearchSuggestionType.AREA_BRICK -> {
                updateInstitutionFilter(_activeFilter.value.institutionFilter.copy(areaBrick = suggestion.text))
            }
            SearchSuggestionType.WARD_NAME -> {
                updateWardFilter(_activeFilter.value.wardFilter.copy(nameQuery = suggestion.text))
            }
            SearchSuggestionType.DESIGNATION -> {
                updateAssignmentFilter(_activeFilter.value.assignmentFilter.copy(designation = suggestion.text))
            }
            SearchSuggestionType.RECENT_SEARCH -> {
                _globalSearchQuery.value = suggestion.text
                performGlobalSearch()
            }
            SearchSuggestionType.SAVED_FILTER -> {
                // Would need filter ID from metadata
                suggestion.metadata["filterId"]?.toLongOrNull()?.let { filterId ->
                    applySavedFilter(filterId)
                }
            }
        }
        
        _searchSuggestions.value = emptyList()
    }
    
    // ========== VALIDATION ==========
    
    /**
     * Validates the current search filter
     */
    fun validateCurrentFilter(): FilterValidationResult {
        return searchRepository.validateFilter(_activeFilter.value).let { result ->
            // This would be a suspend function in real implementation
            FilterValidationResult(result.isValid, result.errors, result.warnings)
        }
    }
    
    /**
     * Clears any error or message state
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }
    
    // ========== ANALYTICS ==========
    
    /**
     * Gets search metrics
     */
    fun getSearchMetrics() {
        viewModelScope.launch {
            try {
                val metrics = searchRepository.getSearchMetrics()
                _uiState.value = _uiState.value.copy(searchMetrics = metrics)
            } catch (e: Exception) {
                // Ignore metrics errors
            }
        }
    }
}

/**
 * UI state for search-related screens
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val lastSearchTime: Long = 0,
    val searchMetrics: SearchMetrics? = null
)

/**
 * Entity types that can be searched
 */
enum class SearchEntityType {
    DOCTORS,
    INSTITUTIONS,
    WARDS,
    ASSIGNMENTS
}