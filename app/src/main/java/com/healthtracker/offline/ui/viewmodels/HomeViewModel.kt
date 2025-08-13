package com.healthtracker.offline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.healthtracker.offline.data.repository.DoctorRepository
import com.healthtracker.offline.data.repository.InstitutionRepository
import com.healthtracker.offline.data.repository.SearchRepository
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.models.GlobalSearchFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the home screen dashboard.
 * 
 * Provides overview statistics, recent updates, and quick actions.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    private val institutionRepository: InstitutionRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {
    
    // ========== UI STATE ==========
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _quickSearchQuery = MutableStateFlow("")
    val quickSearchQuery: StateFlow<String> = _quickSearchQuery.asStateFlow()
    
    // ========== DASHBOARD DATA ==========
    
    val dashboardStats: StateFlow<DashboardStats> = combine(
        doctorRepository.getAllDoctors(),
        institutionRepository.getAllInstitutions(),
        doctorRepository.getDoctorsWithAssignmentCount(),
        institutionRepository.getInstitutionsWithWardCount()
    ) { doctors, institutions, doctorsWithAssignments, institutionsWithWards ->
        DashboardStats(
            totalDoctors = doctors.size,
            totalInstitutions = institutions.size,
            totalAssignments = doctorsWithAssignments.sumOf { it.assignmentCount },
            totalWards = institutionsWithWards.sumOf { it.actualWardCount },
            doctorsWithAssignments = doctorsWithAssignments.count { it.assignmentCount > 0 },
            institutionsWithWards = institutionsWithWards.count { it.actualWardCount > 0 }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )
    
    val recentDoctors: StateFlow<List<Doctor>> = doctorRepository.getAllDoctors()
        .map { doctors -> doctors.take(5) } // Show last 5 added doctors
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val recentInstitutions: StateFlow<List<Institution>> = institutionRepository.getAllInstitutions()
        .map { institutions -> institutions.take(5) } // Show last 5 added institutions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val topSpecialities: StateFlow<List<SpecialityCount>> = doctorRepository.getAllDoctors()
        .map { doctors ->
            doctors.groupBy { it.speciality }
                .map { (speciality, doctorList) ->
                    SpecialityCount(speciality, doctorList.size)
                }
                .sortedByDescending { it.count }
                .take(5)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val topAreaBricks: StateFlow<List<AreaBrickCount>> = institutionRepository.getAllInstitutions()
        .map { institutions ->
            institutions.groupBy { it.areaBrick }
                .map { (areaBrick, institutionList) ->
                    AreaBrickCount(areaBrick, institutionList.size)
                }
                .sortedByDescending { it.count }
                .take(5)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== QUICK SEARCH ==========
    
    val quickSearchResults: StateFlow<QuickSearchResults> = _quickSearchQuery
        .debounce(300) // Debounce search queries
        .flatMapLatest { query ->
            if (query.isBlank() || query.length < 2) {
                flowOf(QuickSearchResults())
            } else {
                performQuickSearch(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuickSearchResults()
        )
    
    // ========== ACTIONS ==========
    
    /**
     * Updates the quick search query
     */
    fun updateQuickSearchQuery(query: String) {
        _quickSearchQuery.value = query
    }
    
    /**
     * Clears the quick search
     */
    fun clearQuickSearch() {
        _quickSearchQuery.value = ""
    }
    
    /**
     * Refreshes dashboard data
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                // Trigger data refresh by collecting latest values
                dashboardStats.first()
                recentDoctors.first()
                recentInstitutions.first()
                
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    lastRefreshTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Failed to refresh dashboard"
                )
            }
        }
    }
    
    /**
     * Performs a quick search across doctors and institutions
     */
    private fun performQuickSearch(query: String): Flow<QuickSearchResults> {
        return flow {
            try {
                val filter = GlobalSearchFilter.fromQuery(query)
                val results = searchRepository.globalSearch(filter).getOrNull()
                
                if (results != null) {
                    emit(
                        QuickSearchResults(
                            doctors = results.doctors.take(3),
                            institutions = results.institutions.take(3),
                            totalResults = results.totalResults,
                            hasMoreResults = results.totalResults > 6
                        )
                    )
                } else {
                    emit(QuickSearchResults())
                }
            } catch (e: Exception) {
                emit(QuickSearchResults())
            }
        }
    }
    
    /**
     * Gets formatted statistics summary
     */
    fun getStatsSummary(): StateFlow<String> {
        return dashboardStats.map { stats ->
            buildString {
                append("${stats.totalDoctors} doctors")
                append(" • ${stats.totalInstitutions} institutions")
                append(" • ${stats.totalAssignments} assignments")
                if (stats.totalWards > 0) {
                    append(" • ${stats.totalWards} wards")
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    }
    
    /**
     * Clears any error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for the home screen
 */
data class HomeUiState(
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastRefreshTime: Long = 0
)

/**
 * Dashboard statistics data
 */
data class DashboardStats(
    val totalDoctors: Int = 0,
    val totalInstitutions: Int = 0,
    val totalAssignments: Int = 0,
    val totalWards: Int = 0,
    val doctorsWithAssignments: Int = 0,
    val institutionsWithWards: Int = 0
) {
    val doctorsWithoutAssignments: Int
        get() = totalDoctors - doctorsWithAssignments
    
    val institutionsWithoutWards: Int
        get() = totalInstitutions - institutionsWithWards
    
    val averageAssignmentsPerDoctor: Double
        get() = if (totalDoctors > 0) totalAssignments.toDouble() / totalDoctors else 0.0
    
    val averageWardsPerInstitution: Double
        get() = if (totalInstitutions > 0) totalWards.toDouble() / totalInstitutions else 0.0
}

/**
 * Quick search results
 */
data class QuickSearchResults(
    val doctors: List<Doctor> = emptyList(),
    val institutions: List<Institution> = emptyList(),
    val totalResults: Int = 0,
    val hasMoreResults: Boolean = false
)

/**
 * Speciality count for dashboard
 */
data class SpecialityCount(
    val speciality: String,
    val count: Int
)

/**
 * Area brick count for dashboard
 */
data class AreaBrickCount(
    val areaBrick: String,
    val count: Int
)