package com.healthtracker.offline.data.repository

import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward
import com.healthtracker.offline.data.dao.InstitutionDao
import com.healthtracker.offline.data.dao.WardDao
import com.healthtracker.offline.data.dao.DoctorInstitutionDao
import com.healthtracker.offline.data.dao.InstitutionWithWardCount
import com.healthtracker.offline.data.dao.InstitutionWithDoctorCount
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of InstitutionRepository using Room database.
 * 
 * Provides concrete implementation of institution and ward-related operations
 * with proper error handling and data validation.
 */
@Singleton
class InstitutionRepositoryImpl @Inject constructor(
    private val institutionDao: InstitutionDao,
    private val wardDao: WardDao,
    private val doctorInstitutionDao: DoctorInstitutionDao
) : InstitutionRepository {
    
    // ========== INSTITUTION OPERATIONS ==========
    
    override suspend fun addInstitution(institution: Institution): Result<Long> {
        return try {
            validateInstitution(institution).getOrThrow()
            val institutionId = institutionDao.insertInstitution(institution)
            Result.success(institutionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addInstitutions(institutions: List<Institution>): Result<List<Long>> {
        return try {
            institutions.forEach { institution ->
                validateInstitution(institution).getOrThrow()
            }
            val institutionIds = institutionDao.insertInstitutions(institutions)
            Result.success(institutionIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllInstitutions(): Flow<List<Institution>> {
        return institutionDao.getAllInstitutions()
    }
    
    override suspend fun getInstitutionById(institutionId: Int): Result<Institution> {
        return try {
            val institution = institutionDao.getInstitutionById(institutionId)
            if (institution != null) {
                Result.success(institution)
            } else {
                Result.failure(Exception("Institution not found with ID: $institutionId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun searchInstitutionsByName(searchQuery: String): Flow<List<Institution>> {
        return institutionDao.searchInstitutionsByName(searchQuery)
    }
    
    override fun getInstitutionsByAreaBrick(areaBrick: String): Flow<List<Institution>> {
        return institutionDao.getInstitutionsByAreaBrick(areaBrick)
    }
    
    override fun getInstitutionsSortedByAreaBrick(): Flow<List<Institution>> {
        return institutionDao.getInstitutionsSortedByAreaBrick()
    }
    
    override fun getAllAreaBricks(): Flow<List<String>> {
        return institutionDao.getAllAreaBricks()
    }
    
    override fun getAllSegmentNames(): Flow<List<String>> {
        return institutionDao.getAllSegmentNames()
    }
    
    override fun searchInstitutions(
        nameQuery: String?,
        areaBrick: String?,
        segmentName: String?
    ): Flow<List<Institution>> {
        val name = nameQuery?.let { "%$it%" } ?: "%"
        val area = areaBrick ?: "%"
        val segment = segmentName ?: "%"
        return institutionDao.searchInstitutions(name, area, segment)
    }
    
    override fun getInstitutionsWithWardCount(): Flow<List<InstitutionWithWardCount>> {
        return institutionDao.getInstitutionsWithWardCount()
    }
    
    override fun getInstitutionsWithDoctorCount(): Flow<List<InstitutionWithDoctorCount>> {
        return institutionDao.getInstitutionsWithDoctorCount()
    }
    
    override suspend fun getInstitutionCount(): Int {
        return try {
            institutionDao.getInstitutionCount()
        } catch (e: Exception) {
            0
        }
    }
    
    override suspend fun updateInstitution(institution: Institution): Result<Unit> {
        return try {
            validateInstitution(institution).getOrThrow()
            val rowsAffected = institutionDao.updateInstitution(institution)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Institution not found or no changes made"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteInstitution(institution: Institution): Result<Unit> {
        return deleteInstitutionById(institution.institutionId)
    }
    
    override suspend fun deleteInstitutionById(institutionId: Int): Result<Unit> {
        return try {
            val canDelete = canDeleteInstitution(institutionId).getOrThrow()
            if (!canDelete) {
                return Result.failure(Exception("Cannot delete institution with wards or doctor assignments"))
            }
            
            val rowsAffected = institutionDao.deleteInstitutionById(institutionId)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Institution not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun canDeleteInstitution(institutionId: Int): Result<Boolean> {
        return try {
            val hasWards = institutionDao.hasWards(institutionId)
            val hasAssignments = institutionDao.hasDoctorAssignments(institutionId)
            Result.success(!hasWards && !hasAssignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== WARD OPERATIONS ==========
    
    override suspend fun addWard(ward: Ward): Result<Long> {
        return try {
            validateWard(ward).getOrThrow()
            val wardId = wardDao.insertWard(ward)
            
            // Update institution ward count
            updateInstitutionWardCount(ward.institutionId)
            
            Result.success(wardId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addWards(wards: List<Ward>): Result<List<Long>> {
        return try {
            wards.forEach { ward ->
                validateWard(ward).getOrThrow()
            }
            val wardIds = wardDao.insertWards(wards)
            
            // Update ward counts for affected institutions
            wards.map { it.institutionId }.distinct().forEach { institutionId ->
                updateInstitutionWardCount(institutionId)
            }
            
            Result.success(wardIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getWardsByInstitution(institutionId: Int): Flow<List<Ward>> {
        return wardDao.getWardsByInstitution(institutionId)
    }
    
    override suspend fun getWardById(wardId: Int): Result<Ward> {
        return try {
            val ward = wardDao.getWardById(wardId)
            if (ward != null) {
                Result.success(ward)
            } else {
                Result.failure(Exception("Ward not found with ID: $wardId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun searchWardsByName(searchQuery: String): Flow<List<Ward>> {
        return wardDao.searchWardsByName(searchQuery)
    }
    
    override fun getWardsByOpdDay(day: String): Flow<List<Ward>> {
        return wardDao.getWardsByOpdDay(day)
    }
    
    override fun getWardsByOtDay(day: String): Flow<List<Ward>> {
        return wardDao.getWardsByOtDay(day)
    }
    
    override suspend fun getWardCountByInstitution(institutionId: Int): Int {
        return try {
            wardDao.getWardCountByInstitution(institutionId)
        } catch (e: Exception) {
            0
        }
    }
    
    override suspend fun updateWard(ward: Ward): Result<Unit> {
        return try {
            validateWard(ward).getOrThrow()
            val rowsAffected = wardDao.updateWard(ward)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ward not found or no changes made"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteWard(ward: Ward): Result<Unit> {
        return deleteWardById(ward.wardId)
    }
    
    override suspend fun deleteWardById(wardId: Int): Result<Unit> {
        return try {
            val canDelete = canDeleteWard(wardId).getOrThrow()
            if (!canDelete) {
                return Result.failure(Exception("Cannot delete ward with doctor assignments"))
            }
            
            // Get ward info before deletion for updating institution count
            val ward = wardDao.getWardById(wardId)
            val institutionId = ward?.institutionId
            
            val rowsAffected = wardDao.deleteWardById(wardId)
            if (rowsAffected > 0) {
                // Update institution ward count
                institutionId?.let { updateInstitutionWardCount(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ward not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun canDeleteWard(wardId: Int): Result<Boolean> {
        return try {
            val hasAssignments = wardDao.hasDoctorAssignments(wardId)
            Result.success(!hasAssignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== VALIDATION ==========
    
    override suspend fun validateInstitution(institution: Institution): Result<Unit> {
        return try {
            when {
                institution.name.isBlank() -> 
                    Result.failure(Exception("Institution name is required"))
                institution.areaBrick.isBlank() -> 
                    Result.failure(Exception("Area brick is required"))
                institution.numberOfWards < 0 -> 
                    Result.failure(Exception("Number of wards cannot be negative"))
                !institution.isValid() -> 
                    Result.failure(Exception("Invalid institution data"))
                else -> Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun validateWard(ward: Ward): Result<Unit> {
        return try {
            when {
                ward.wardName.isBlank() -> 
                    Result.failure(Exception("Ward name is required"))
                ward.institutionId <= 0 -> 
                    Result.failure(Exception("Valid institution ID is required"))
                !ward.isValid() -> 
                    Result.failure(Exception("Invalid ward data"))
                else -> Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== COMPLEX OPERATIONS ==========
    
    override suspend fun createInstitutionWithWards(
        institution: Institution,
        wards: List<Ward>
    ): Result<Long> {
        return try {
            validateInstitution(institution).getOrThrow()
            wards.forEach { ward ->
                validateWard(ward.copy(institutionId = 0)).getOrThrow() // Validate without institution ID
            }
            
            val institutionId = institutionDao.insertInstitution(institution)
            
            if (wards.isNotEmpty()) {
                val wardsWithInstitutionId = wards.map { it.copy(institutionId = institutionId.toInt()) }
                wardDao.insertWards(wardsWithInstitutionId)
                
                // Update institution ward count
                updateInstitutionWardCount(institutionId.toInt())
            }
            
            Result.success(institutionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateInstitutionWardCount(institutionId: Int): Result<Unit> {
        return try {
            val actualWardCount = wardDao.getWardCountByInstitution(institutionId)
            val institution = institutionDao.getInstitutionById(institutionId)
            
            if (institution != null && institution.numberOfWards != actualWardCount) {
                val updatedInstitution = institution.copy(numberOfWards = actualWardCount)
                institutionDao.updateInstitution(updatedInstitution)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}