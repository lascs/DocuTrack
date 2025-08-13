package com.healthtracker.offline.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.dao.DoctorDao
import com.healthtracker.offline.data.dao.DoctorInstitutionDao
import com.healthtracker.offline.data.dao.DoctorWithAssignmentCount
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DoctorRepository using Room database.
 * 
 * Provides concrete implementation of doctor-related operations
 * with proper error handling and data validation.
 */
@Singleton
class DoctorRepositoryImpl @Inject constructor(
    private val doctorDao: DoctorDao,
    private val doctorInstitutionDao: DoctorInstitutionDao
) : DoctorRepository {
    
    // ========== CREATE ==========
    
    override suspend fun addDoctor(doctor: Doctor): Result<Long> {
        return try {
            // Validate doctor data
            validateDoctor(doctor).getOrThrow()
            
            // Check PMDC number uniqueness
            val isUnique = isPmdcNumberUnique(doctor.pmdcNumber).getOrThrow()
            if (!isUnique) {
                return Result.failure(Exception("PMDC number already exists"))
            }
            
            val doctorId = doctorDao.insertDoctor(doctor)
            Result.success(doctorId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addDoctors(doctors: List<Doctor>): Result<List<Long>> {
        return try {
            // Validate all doctors
            doctors.forEach { doctor ->
                validateDoctor(doctor).getOrThrow()
            }
            
            // Check for duplicate PMDC numbers within the list
            val pmdcNumbers = doctors.map { it.pmdcNumber }
            if (pmdcNumbers.size != pmdcNumbers.distinct().size) {
                return Result.failure(Exception("Duplicate PMDC numbers in the list"))
            }
            
            // Check PMDC number uniqueness against database
            pmdcNumbers.forEach { pmdcNumber ->
                val isUnique = isPmdcNumberUnique(pmdcNumber).getOrThrow()
                if (!isUnique) {
                    return Result.failure(Exception("PMDC number $pmdcNumber already exists"))
                }
            }
            
            val doctorIds = doctorDao.insertDoctors(doctors)
            Result.success(doctorIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== READ ==========
    
    override fun getAllDoctors(): Flow<List<Doctor>> {
        return doctorDao.getAllDoctors()
    }
    
    override suspend fun getDoctorById(doctorId: Int): Result<Doctor> {
        return try {
            val doctor = doctorDao.getDoctorById(doctorId)
            if (doctor != null) {
                Result.success(doctor)
            } else {
                Result.failure(Exception("Doctor not found with ID: $doctorId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDoctorByPmdcNumber(pmdcNumber: String): Result<Doctor> {
        return try {
            val doctor = doctorDao.getDoctorByPmdcNumber(pmdcNumber)
            if (doctor != null) {
                Result.success(doctor)
            } else {
                Result.failure(Exception("Doctor not found with PMDC number: $pmdcNumber"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun searchDoctorsByName(searchQuery: String): Flow<List<Doctor>> {
        return doctorDao.searchDoctorsByName(searchQuery)
    }
    
    override fun getDoctorsBySpeciality(speciality: String): Flow<List<Doctor>> {
        return doctorDao.getDoctorsBySpeciality(speciality)
    }
    
    override fun getAllSpecialities(): Flow<List<String>> {
        return doctorDao.getAllSpecialities()
    }
    
    override fun searchDoctors(nameQuery: String?, speciality: String?): Flow<List<Doctor>> {
        val name = nameQuery?.let { "%$it%" } ?: "%"
        val spec = speciality ?: "%"
        return doctorDao.searchDoctors(name, spec)
    }
    
    override fun getDoctorsWithAssignmentCount(): Flow<List<DoctorWithAssignmentCount>> {
        return doctorDao.getDoctorsWithAssignmentCount()
    }
    
    override suspend fun getDoctorCount(): Int {
        return try {
            doctorDao.getDoctorCount()
        } catch (e: Exception) {
            0
        }
    }
    
    // ========== UPDATE ==========
    
    override suspend fun updateDoctor(doctor: Doctor): Result<Unit> {
        return try {
            // Validate doctor data
            validateDoctor(doctor).getOrThrow()
            
            // Check PMDC number uniqueness (excluding current doctor)
            val isUnique = isPmdcNumberUnique(doctor.pmdcNumber, doctor.doctorId).getOrThrow()
            if (!isUnique) {
                return Result.failure(Exception("PMDC number already exists"))
            }
            
            val rowsAffected = doctorDao.updateDoctor(doctor)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Doctor not found or no changes made"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== DELETE ==========
    
    override suspend fun deleteDoctor(doctor: Doctor): Result<Unit> {
        return deleteDoctorById(doctor.doctorId)
    }
    
    override suspend fun deleteDoctorById(doctorId: Int): Result<Unit> {
        return try {
            // Check if doctor can be safely deleted
            val canDelete = canDeleteDoctor(doctorId).getOrThrow()
            if (!canDelete) {
                return Result.failure(Exception("Cannot delete doctor with active assignments"))
            }
            
            val rowsAffected = doctorDao.deleteDoctorById(doctorId)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Doctor not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun canDeleteDoctor(doctorId: Int): Result<Boolean> {
        return try {
            val assignmentCount = doctorInstitutionDao.getAssignmentCountByDoctor(doctorId)
            Result.success(assignmentCount == 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== VALIDATION ==========
    
    override suspend fun validateDoctor(doctor: Doctor): Result<Unit> {
        return try {
            when {
                doctor.name.isBlank() -> 
                    Result.failure(Exception("Doctor name is required"))
                doctor.speciality.isBlank() -> 
                    Result.failure(Exception("Speciality is required"))
                doctor.pmdcNumber.isBlank() -> 
                    Result.failure(Exception("PMDC number is required"))
                !doctor.isValid() -> 
                    Result.failure(Exception("Invalid doctor data"))
                else -> Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isPmdcNumberUnique(pmdcNumber: String, excludeDoctorId: Int?): Result<Boolean> {
        return try {
            val exists = doctorDao.isPmdcNumberExists(pmdcNumber, excludeDoctorId)
            Result.success(!exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}