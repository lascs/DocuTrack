package com.healthtracker.offline.data.repository

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
import com.healthtracker.offline.data.dao.DoctorDao
import com.healthtracker.offline.data.dao.DoctorInstitutionDao

/**
 * Unit tests for DoctorRepositoryImpl
 */
class DoctorRepositoryImplTest {
    
    @Mock
    private lateinit var doctorDao: DoctorDao
    
    @Mock
    private lateinit var doctorInstitutionDao: DoctorInstitutionDao
    
    private lateinit var repository: DoctorRepositoryImpl
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = DoctorRepositoryImpl(doctorDao, doctorInstitutionDao)
    }
    
    @Test
    fun `addDoctor should return success when doctor is valid and PMDC is unique`() = runTest {
        // Given
        val doctor = Doctor(
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        whenever(doctorDao.isPmdcNumberExists(doctor.pmdcNumber, null)).thenReturn(false)
        whenever(doctorDao.insertDoctor(doctor)).thenReturn(1L)
        
        // When
        val result = repository.addDoctor(doctor)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        verify(doctorDao).insertDoctor(doctor)
    }
    
    @Test
    fun `addDoctor should return failure when PMDC number already exists`() = runTest {
        // Given
        val doctor = Doctor(
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        whenever(doctorDao.isPmdcNumberExists(doctor.pmdcNumber, null)).thenReturn(true)
        
        // When
        val result = repository.addDoctor(doctor)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("PMDC number already exists") == true)
    }
    
    @Test
    fun `addDoctor should return failure when doctor name is blank`() = runTest {
        // Given
        val doctor = Doctor(
            name = "",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        
        // When
        val result = repository.addDoctor(doctor)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Doctor name is required") == true)
    }
    
    @Test
    fun `getDoctorById should return success when doctor exists`() = runTest {
        // Given
        val doctorId = 1
        val doctor = Doctor(
            doctorId = doctorId,
            name = "Dr. John Doe",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        whenever(doctorDao.getDoctorById(doctorId)).thenReturn(doctor)
        
        // When
        val result = repository.getDoctorById(doctorId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(doctor, result.getOrNull())
    }
    
    @Test
    fun `getDoctorById should return failure when doctor does not exist`() = runTest {
        // Given
        val doctorId = 1
        whenever(doctorDao.getDoctorById(doctorId)).thenReturn(null)
        
        // When
        val result = repository.getDoctorById(doctorId)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Doctor not found") == true)
    }
    
    @Test
    fun `canDeleteDoctor should return true when doctor has no assignments`() = runTest {
        // Given
        val doctorId = 1
        whenever(doctorInstitutionDao.getAssignmentCountByDoctor(doctorId)).thenReturn(0)
        
        // When
        val result = repository.canDeleteDoctor(doctorId)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }
    
    @Test
    fun `canDeleteDoctor should return false when doctor has assignments`() = runTest {
        // Given
        val doctorId = 1
        whenever(doctorInstitutionDao.getAssignmentCountByDoctor(doctorId)).thenReturn(2)
        
        // When
        val result = repository.canDeleteDoctor(doctorId)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == false)
    }
    
    @Test
    fun `updateDoctor should return success when doctor is valid and update succeeds`() = runTest {
        // Given
        val doctor = Doctor(
            doctorId = 1,
            name = "Dr. John Doe Updated",
            speciality = "Cardiology",
            pmdcNumber = "12345",
            mobileNumber = "03001234567"
        )
        whenever(doctorDao.isPmdcNumberExists(doctor.pmdcNumber, doctor.doctorId)).thenReturn(false)
        whenever(doctorDao.updateDoctor(doctor)).thenReturn(1)
        
        // When
        val result = repository.updateDoctor(doctor)
        
        // Then
        assertTrue(result.isSuccess)
        verify(doctorDao).updateDoctor(doctor)
    }
}