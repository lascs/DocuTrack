package com.healthtracker.offline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.entities.DoctorInstitution
import com.healthtracker.offline.data.repository.DoctorRepository
import com.healthtracker.offline.data.dao.DoctorWithAssignmentCount
import com.healthtracker.offline.data.dao.DoctorAssignmentWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing doctor-related UI state and business logic.
 * 
 * Handles doctor CRUD operations, assignment management, and UI state.
 */
@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository
) : ViewModel() {
    
    // ========== UI STATE ==========
    
    private val _uiState = MutableStateFlow(DoctorUiState())
    val uiState: StateFlow<DoctorUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedSpeciality = MutableStateFlow<String?>(null)
    val selectedSpeciality: StateFlow<String?> = _selectedSpeciality.asStateFlow()
    
    // ========== DATA FLOWS ==========
    
    val doctors: StateFlow<List<Doctor>> = combine(
        doctorRepository.getAllDoctors(),
        _searchQuery,
        _selectedSpeciality
    ) { allDoctors, query, speciality ->
        var filteredDoctors = allDoctors
        
        if (query.isNotBlank()) {
            filteredDoctors = filteredDoctors.filter { doctor ->
                doctor.name.contains(query, ignoreCase = true) ||
                doctor.pmdcNumber.contains(query, ignoreCase = true) ||
                doctor.mobileNumber.contains(query, ignoreCase = true)
            }
        }
        
        if (speciality != null) {
            filteredDoctors = filteredDoctors.filter { doctor ->
                doctor.speciality.equals(speciality, ignoreCase = true)
            }
        }
        
        filteredDoctors
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val doctorsWithAssignmentCount: StateFlow<List<DoctorWithAssignmentCount>> = 
        doctorRepository.getDoctorsWithAssignmentCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    val specialities: StateFlow<List<String>> = doctorRepository.getAllSpecialities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== ACTIONS ==========
    
    /**
     * Updates the search query for filtering doctors
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Updates the selected speciality filter
     */
    fun updateSelectedSpeciality(speciality: String?) {
        _selectedSpeciality.value = speciality
    }
    
    /**
     * Clears all filters
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedSpeciality.value = null
    }
    
    /**
     * Adds a new doctor
     */
    fun addDoctor(doctor: Doctor) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            doctorRepository.addDoctor(doctor)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Doctor added successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to add doctor"
                    )
                }
        }
    }
    
    /**
     * Updates an existing doctor
     */
    fun updateDoctor(doctor: Doctor) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            doctorRepository.updateDoctor(doctor)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Doctor updated successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update doctor"
                    )
                }
        }
    }
    
    /**
     * Deletes a doctor
     */
    fun deleteDoctor(doctor: Doctor) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Check if doctor can be safely deleted
            doctorRepository.canDeleteDoctor(doctor.doctorId)
                .onSuccess { canDelete ->
                    if (canDelete) {
                        doctorRepository.deleteDoctor(doctor)
                            .onSuccess {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    message = "Doctor deleted successfully"
                                )
                            }
                            .onFailure { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to delete doctor"
                                )
                            }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cannot delete doctor with active assignments"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to check doctor assignments"
                    )
                }
        }
    }
    
    /**
     * Gets a doctor by ID
     */
    fun getDoctorById(doctorId: Int): StateFlow<Doctor?> {
        return doctors.map { doctorList ->
            doctorList.find { it.doctorId == doctorId }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }
    
    /**
     * Validates doctor data
     */
    fun validateDoctor(doctor: Doctor): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (doctor.name.isBlank()) {
            errors.add("Doctor name is required")
        }
        
        if (doctor.speciality.isBlank()) {
            errors.add("Speciality is required")
        }
        
        if (doctor.pmdcNumber.isBlank()) {
            errors.add("PMDC number is required")
        }
        
        if (doctor.mobileNumber.isBlank()) {
            errors.add("Mobile number is required")
        } else if (!isValidMobileNumber(doctor.mobileNumber)) {
            errors.add("Invalid mobile number format")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Checks if PMDC number is unique
     */
    fun checkPmdcNumberUnique(pmdcNumber: String, excludeDoctorId: Int? = null) {
        viewModelScope.launch {
            doctorRepository.isPmdcNumberUnique(pmdcNumber, excludeDoctorId)
                .onSuccess { isUnique ->
                    _uiState.value = _uiState.value.copy(
                        isPmdcNumberUnique = isUnique
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isPmdcNumberUnique = true // Default to true on error
                    )
                }
        }
    }
    
    /**
     * Clears any error or message state
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }
    
    // ========== HELPER METHODS ==========
    
    private fun isValidMobileNumber(mobile: String): Boolean {
        val cleanNumber = mobile.replace(Regex("[^0-9]"), "")
        return cleanNumber.length >= 10 && cleanNumber.length <= 15
    }
}

/**
 * UI state for doctor-related screens
 */
data class DoctorUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val isPmdcNumberUnique: Boolean = true
)

/**
 * Validation result for doctor data
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String {
        return errors.joinToString("\n")
    }
}