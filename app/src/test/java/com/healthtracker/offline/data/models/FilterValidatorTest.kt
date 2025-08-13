package com.healthtracker.offline.data.models

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FilterValidator
 */
class FilterValidatorTest {
    
    @Test
    fun `validateDoctorFilter should return valid for empty filter`() {
        // Given
        val filter = DoctorSearchFilter.empty()
        
        // When
        val result = FilterValidator.validateDoctorFilter(filter)
        
        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
        assertTrue(result.warnings.isEmpty())
    }
    
    @Test
    fun `validateDoctorFilter should return warning for single character name`() {
        // Given
        val filter = DoctorSearchFilter(nameQuery = "A")
        
        // When
        val result = FilterValidator.validateDoctorFilter(filter)
        
        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(result.warnings.any { it.contains("Single character") })
    }
    
    @Test
    fun `validateDoctorFilter should return error for invalid mobile number`() {
        // Given
        val filter = DoctorSearchFilter(mobileNumber = "invalid-mobile")
        
        // When
        val result = FilterValidator.validateDoctorFilter(filter)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.any { it.contains("Mobile number contains invalid characters") })
    }
    
    @Test
    fun `validateDoctorFilter should return error for negative assignment count`() {
        // Given
        val filter = DoctorSearchFilter(assignmentCount = -1..5)
        
        // When
        val result = FilterValidator.validateDoctorFilter(filter)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.any { it.contains("Assignment count cannot be negative") })
    }
    
    @Test
    fun `validateDoctorFilter should return error for invalid assignment count range`() {
        // Given
        val filter = DoctorSearchFilter(assignmentCount = 5..2)
        
        // When
        val result = FilterValidator.validateDoctorFilter(filter)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.any { it.contains("Assignment count range is invalid") })
    }
    
    @Test
    fun `validateWardFilter should return error for invalid OPD day`() {
        // Given
        val filter = WardSearchFilter(opdDays = listOf("InvalidDay"))
        
        // When
        val result = FilterValidator.validateWardFilter(filter)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.any { it.contains("Invalid OPD day: InvalidDay") })
    }
    
    @Test
    fun `validateWardFilter should return valid for valid days`() {
        // Given
        val filter = WardSearchFilter(
            opdDays = listOf("Monday", "Tuesday"),
            otDays = listOf("Wednesday", "Thursday"),
            hasOpdOnDay = "Friday",
            hasOtOnDay = "Saturday"
        )
        
        // When
        val result = FilterValidator.validateWardFilter(filter)
        
        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `validateInstitutionFilter should return warning for very high ward count`() {
        // Given
        val filter = InstitutionSearchFilter(wardCountRange = 500..2000)
        
        // When
        val result = FilterValidator.validateInstitutionFilter(filter)
        
        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(result.warnings.any { it.contains("Very high ward count") })
    }
    
    @Test
    fun `validateAssignmentFilter should return error for invalid duty day`() {
        // Given
        val filter = AssignmentSearchFilter(dutyDays = listOf("InvalidDay"))
        
        // When
        val result = FilterValidator.validateAssignmentFilter(filter)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.any { it.contains("Invalid duty day: InvalidDay") })
    }
    
    @Test
    fun `validateGlobalFilter should validate sub-filters`() {
        // Given
        val globalFilter = GlobalSearchFilter(
            query = "test",
            doctorFilter = DoctorSearchFilter(mobileNumber = "invalid-mobile"),
            wardFilter = WardSearchFilter(opdDays = listOf("InvalidDay"))
        )
        
        // When
        val result = FilterValidator.validateGlobalFilter(globalFilter)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.size >= 2) // Should have errors from both sub-filters
    }
}