package com.healthtracker.offline.ui.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.repository.DoctorRepository
import com.healthtracker.offline.data.dao.DoctorWithAssignmentCount
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers

/**
 * Unit tests for DoctorViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DoctorViewModelTest {
    
    @Mock
    private lateinit var doctorRepository: DoctorRepository
    
    private lateinit var viewModel: DoctorViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mock responses
        whenever(doctorRepository.getAllDoctors()).thenReturn(flowOf(emptyList()))
        whenever(doctorRepository.getDoctorsWithAssignmentCount()).thenReturn(flowOf(emptyList()))
        whenever(doctorRepository.getAllSpecialities()).thenReturn(flowOf(emptyList()))
        
        viewModel = DoctorViewModel(doctorRepository)
    }
    
    @Test
    fun `updateSearchQuery should update search query state`() = runTest {
        // Given
        val query = "Dr. Smith"
        
        // When
        viewModel.updateSearchQuery(query)
        
        // Then
        assertEquals(query, viewModel.searchQuery.value)
    }
    
    @Test
    fun `updateSelectedSpeciality should update speciality filter`() = runTest {
        // Given
        val speciality = "Cardiology"
        
        // When
        viewModel.updateSelectedSpeciality(speciality)
        
        // Then
        assertEquals(speciality, viewModel.selectedSpeciality.value)
    }
    
    @Test
    fun `clearFilters should reset all filters`() = runTest {
        // Given
        viewModel.updateSearchQuery("test")
        viewModel.updateSelectedSpeciality("Cardiology")
        
        // When
        viewModel.clearFilters()
        
        // Then
        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedSpeciality.value)
    }
    
    @Test
    fun `addDoctor should call repository and update UI state on success`() = runTest {
        // Given
        val doctor = Doctor(
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        whenever(doctorRepository.addDoctor(doctor)).thenReturn(Result.success(1L))
        
        // When
        viewModel.addDoctor(doctor)
        
        // Then
        verify(doctorRepository).addDoctor(doctor)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Doctor added successfully", viewModel.uiState.value.message)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `addDoctor should update UI state with error on failure`() = runTest {
        // Given
        val doctor = Doctor(
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        val errorMessage = "PMDC number already exists"
        whenever(doctorRepository.addDoctor(doctor)).thenReturn(Result.failure(Exception(errorMessage)))
        
        // When
        viewModel.addDoctor(doctor)
        
        // Then
        verify(doctorRepository).addDoctor(doctor)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.message)
    }
    
    @Test
    fun `deleteDoctor should check if doctor can be deleted before deletion`() = runTest {
        // Given
        val doctor = Doctor(
            doctorId = 1,
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        whenever(doctorRepository.canDeleteDoctor(1)).thenReturn(Result.success(true))
        whenever(doctorRepository.deleteDoctor(doctor)).thenReturn(Result.success(Unit))
        
        // When
        viewModel.deleteDoctor(doctor)
        
        // Then
        verify(doctorRepository).canDeleteDoctor(1)
        verify(doctorRepository).deleteDoctor(doctor)
        assertEquals("Doctor deleted successfully", viewModel.uiState.value.message)
    }
    
    @Test
    fun `deleteDoctor should show error if doctor has active assignments`() = runTest {
        // Given
        val doctor = Doctor(
            doctorId = 1,
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        whenever(doctorRepository.canDeleteDoctor(1)).thenReturn(Result.success(false))
        
        // When
        viewModel.deleteDoctor(doctor)
        
        // Then
        verify(doctorRepository).canDeleteDoctor(1)
        assertEquals("Cannot delete doctor with active assignments", viewModel.uiState.value.error)
    }
    
    @Test
    fun `validateDoctor should return valid result for valid doctor`() = runTest {
        // Given
        val doctor = Doctor(
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        
        // When
        val result = viewModel.validateDoctor(doctor)
        
        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `validateDoctor should return invalid result for doctor with missing name`() = runTest {
        // Given
        val doctor = Doctor(
            name = "",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        
        // When
        val result = viewModel.validateDoctor(doctor)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Doctor name is required"))
    }
    
    @Test
    fun `validateDoctor should return invalid result for doctor with invalid mobile number`() = runTest {
        // Given
        val doctor = Doctor(
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "123" // Too short
        )
        
        // When
        val result = viewModel.validateDoctor(doctor)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Invalid mobile number format"))
    }
    
    @Test
    fun `checkPmdcNumberUnique should update UI state with uniqueness result`() = runTest {
        // Given
        val pmdcNumber = "12345"
        whenever(doctorRepository.isPmdcNumberUnique(pmdcNumber, null)).thenReturn(Result.success(true))
        
        // When
        viewModel.checkPmdcNumberUnique(pmdcNumber)
        
        // Then
        verify(doctorRepository).isPmdcNumberUnique(pmdcNumber, null)
        assertTrue(viewModel.uiState.value.isPmdcNumberUnique)
    }
    
    @Test
    fun `clearMessages should clear error and message state`() = runTest {
        // Given
        viewModel.addDoctor(Doctor(name = "", speciality = "", pmdcNumber = "", mobileNumber = ""))
        
        // When
        viewModel.clearMessages()
        
        // Then
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.message)
    }
}