package com.healthtracker.offline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward
import com.healthtracker.offline.data.repository.InstitutionRepository
import com.healthtracker.offline.data.dao.InstitutionWithWardCount
import com.healthtracker.offline.data.dao.InstitutionWithDoctorCount
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing institution and ward-related UI state and business logic.
 * 
 * Handles institution and ward CRUD operations, filtering, and UI state management.
 */
@HiltViewModel
class InstitutionViewModel @Inject constructor(
    private val institutionRepository: InstitutionRepository
) : ViewModel() {
    
    // ========== UI STATE ==========
    
    private val _uiState = MutableStateFlow(InstitutionUiState())
    val uiState: StateFlow<InstitutionUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedAreaBrick = MutableStateFlow<String?>(null)
    val selectedAreaBrick: StateFlow<String?> = _selectedAreaBrick.asStateFlow()
    
    private val _selectedSegment = MutableStateFlow<String?>(null)
    val selectedSegment: StateFlow<String?> = _selectedSegment.asStateFlow()
    
    private val _sortBy = MutableStateFlow(InstitutionSortBy.NAME)
    val sortBy: StateFlow<InstitutionSortBy> = _sortBy.asStateFlow()
    
    // ========== DATA FLOWS ==========
    
    val institutions: StateFlow<List<Institution>> = combine(
        institutionRepository.getAllInstitutions(),
        _searchQuery,
        _selectedAreaBrick,
        _selectedSegment,
        _sortBy
    ) { allInstitutions, query, areaBrick, segment, sortBy ->
        var filteredInstitutions = allInstitutions
        
        // Apply search filter
        if (query.isNotBlank()) {
            filteredInstitutions = filteredInstitutions.filter { institution ->
                institution.name.contains(query, ignoreCase = true) ||
                institution.msName.contains(query, ignoreCase = true) ||
                institution.dmsName.contains(query, ignoreCase = true)
            }
        }
        
        // Apply area brick filter
        if (areaBrick != null) {
            filteredInstitutions = filteredInstitutions.filter { institution ->
                institution.areaBrick.equals(areaBrick, ignoreCase = true)
            }
        }
        
        // Apply segment filter
        if (segment != null) {
            filteredInstitutions = filteredInstitutions.filter { institution ->
                institution.segmentName.equals(segment, ignoreCase = true)
            }
        }
        
        // Apply sorting
        when (sortBy) {
            InstitutionSortBy.NAME -> filteredInstitutions.sortedBy { it.name }
            InstitutionSortBy.AREA_BRICK -> filteredInstitutions.sortedBy { it.areaBrick }
            InstitutionSortBy.WARD_COUNT -> filteredInstitutions.sortedByDescending { it.numberOfWards }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val institutionsWithWardCount: StateFlow<List<InstitutionWithWardCount>> = 
        institutionRepository.getInstitutionsWithWardCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    val institutionsWithDoctorCount: StateFlow<List<InstitutionWithDoctorCount>> = 
        institutionRepository.getInstitutionsWithDoctorCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    val areaBricks: StateFlow<List<String>> = institutionRepository.getAllAreaBricks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val segmentNames: StateFlow<List<String>> = institutionRepository.getAllSegmentNames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== WARD DATA ==========
    
    private val _selectedInstitutionId = MutableStateFlow<Int?>(null)
    val selectedInstitutionId: StateFlow<Int?> = _selectedInstitutionId.asStateFlow()
    
    val wardsForSelectedInstitution: StateFlow<List<Ward>> = _selectedInstitutionId
        .flatMapLatest { institutionId ->
            if (institutionId != null) {
                institutionRepository.getWardsByInstitution(institutionId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== ACTIONS ==========
    
    /**
     * Updates the search query for filtering institutions
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Updates the selected area brick filter
     */
    fun updateSelectedAreaBrick(areaBrick: String?) {
        _selectedAreaBrick.value = areaBrick
    }
    
    /**
     * Updates the selected segment filter
     */
    fun updateSelectedSegment(segment: String?) {
        _selectedSegment.value = segment
    }
    
    /**
     * Updates the sort criteria
     */
    fun updateSortBy(sortBy: InstitutionSortBy) {
        _sortBy.value = sortBy
    }
    
    /**
     * Clears all filters
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedAreaBrick.value = null
        _selectedSegment.value = null
    }
    
    /**
     * Selects an institution to view its wards
     */
    fun selectInstitution(institutionId: Int?) {
        _selectedInstitutionId.value = institutionId
    }
    
    // ========== INSTITUTION OPERATIONS ==========
    
    /**
     * Adds a new institution
     */
    fun addInstitution(institution: Institution) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            institutionRepository.addInstitution(institution)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Institution added successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to add institution"
                    )
                }
        }
    }
    
    /**
     * Updates an existing institution
     */
    fun updateInstitution(institution: Institution) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            institutionRepository.updateInstitution(institution)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Institution updated successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update institution"
                    )
                }
        }
    }
    
    /**
     * Deletes an institution
     */
    fun deleteInstitution(institution: Institution) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Check if institution can be safely deleted
            institutionRepository.canDeleteInstitution(institution.institutionId)
                .onSuccess { canDelete ->
                    if (canDelete) {
                        institutionRepository.deleteInstitution(institution)
                            .onSuccess {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    message = "Institution deleted successfully"
                                )
                            }
                            .onFailure { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to delete institution"
                                )
                            }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cannot delete institution with wards or doctor assignments"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to check institution dependencies"
                    )
                }
        }
    }
    
    /**
     * Creates an institution with its wards in a single operation
     */
    fun createInstitutionWithWards(institution: Institution, wards: List<Ward>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            institutionRepository.createInstitutionWithWards(institution, wards)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Institution and wards created successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create institution with wards"
                    )
                }
        }
    }
    
    // ========== WARD OPERATIONS ==========
    
    /**
     * Adds a new ward to an institution
     */
    fun addWard(ward: Ward) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            institutionRepository.addWard(ward)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Ward added successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to add ward"
                    )
                }
        }
    }
    
    /**
     * Updates an existing ward
     */
    fun updateWard(ward: Ward) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            institutionRepository.updateWard(ward)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Ward updated successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update ward"
                    )
                }
        }
    }
    
    /**
     * Deletes a ward
     */
    fun deleteWard(ward: Ward) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Check if ward can be safely deleted
            institutionRepository.canDeleteWard(ward.wardId)
                .onSuccess { canDelete ->
                    if (canDelete) {
                        institutionRepository.deleteWard(ward)
                            .onSuccess {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    message = "Ward deleted successfully"
                                )
                            }
                            .onFailure { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to delete ward"
                                )
                            }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cannot delete ward with doctor assignments"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to check ward assignments"
                    )
                }
        }
    }
    
    // ========== VALIDATION ==========
    
    /**
     * Validates institution data
     */
    fun validateInstitution(institution: Institution): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (institution.name.isBlank()) {
            errors.add("Institution name is required")
        }
        
        if (institution.areaBrick.isBlank()) {
            errors.add("Area brick is required")
        }
        
        if (institution.numberOfWards < 0) {
            errors.add("Number of wards cannot be negative")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validates ward data
     */
    fun validateWard(ward: Ward): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (ward.wardName.isBlank()) {
            errors.add("Ward name is required")
        }
        
        if (ward.institutionId <= 0) {
            errors.add("Valid institution is required")
        }
        
        // Validate day names
        val validDays = setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        ward.opdDays.forEach { day ->
            if (day !in validDays) {
                errors.add("Invalid OPD day: $day")
            }
        }
        
        ward.otDays.forEach { day ->
            if (day !in validDays) {
                errors.add("Invalid OT day: $day")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Gets an institution by ID
     */
    fun getInstitutionById(institutionId: Int): StateFlow<Institution?> {
        return institutions.map { institutionList ->
            institutionList.find { it.institutionId == institutionId }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }
    
    /**
     * Clears any error or message state
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }
}

/**
 * UI state for institution-related screens
 */
data class InstitutionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

/**
 * Sort options for institutions
 */
enum class InstitutionSortBy {
    NAME,
    AREA_BRICK,
    WARD_COUNT
}