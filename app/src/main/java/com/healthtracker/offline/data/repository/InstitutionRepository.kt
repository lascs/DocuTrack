package com.healthtracker.offline.data.repository

import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward
import com.healthtracker.offline.data.dao.InstitutionWithWardCount
import com.healthtracker.offline.data.dao.InstitutionWithDoctorCount

/**
 * Repository interface for Institution entity operations.
 * 
 * Provides a clean API for institution and ward-related data operations,
 * abstracting the underlying data source implementation.
 */
interface InstitutionRepository {
    
    // ========== INSTITUTION OPERATIONS ==========
    
    /**
     * Adds a new institution to the repository
     * @param institution Institution entity to add
     * @return Result containing the institution ID if successful, or error
     */
    suspend fun addInstitution(institution: Institution): Result<Long>
    
    /**
     * Adds multiple institutions to the repository
     * @param institutions List of institutions to add
     * @return Result containing list of institution IDs if successful, or error
     */
    suspend fun addInstitutions(institutions: List<Institution>): Result<List<Long>>
    
    /**
     * Gets all institutions as a reactive Flow
     * @return Flow of list of all institutions
     */
    fun getAllInstitutions(): Flow<List<Institution>>
    
    /**
     * Gets an institution by ID
     * @param institutionId Institution ID to search for
     * @return Result containing institution if found, or error
     */
    suspend fun getInstitutionById(institutionId: Int): Result<Institution>
    
    /**
     * Searches institutions by name
     * @param searchQuery Search term for institution name
     * @return Flow of matching institutions
     */
    fun searchInstitutionsByName(searchQuery: String): Flow<List<Institution>>
    
    /**
     * Gets institutions filtered by area brick
     * @param areaBrick Area brick to filter by
     * @return Flow of institutions in the specified area brick
     */
    fun getInstitutionsByAreaBrick(areaBrick: String): Flow<List<Institution>>
    
    /**
     * Gets institutions sorted by area brick
     * @return Flow of institutions sorted by area brick, then name
     */
    fun getInstitutionsSortedByAreaBrick(): Flow<List<Institution>>
    
    /**
     * Gets all unique area bricks
     * @return Flow of list of unique area bricks
     */
    fun getAllAreaBricks(): Flow<List<String>>
    
    /**
     * Gets all unique segment names
     * @return Flow of list of unique segment names
     */
    fun getAllSegmentNames(): Flow<List<String>>
    
    /**
     * Searches institutions with multiple criteria
     * @param nameQuery Name search term (optional)
     * @param areaBrick Area brick filter (optional)
     * @param segmentName Segment name filter (optional)
     * @return Flow of matching institutions
     */
    fun searchInstitutions(
        nameQuery: String? = null,
        areaBrick: String? = null,
        segmentName: String? = null
    ): Flow<List<Institution>>
    
    /**
     * Gets institutions with their ward count
     * @return Flow of institutions with ward counts
     */
    fun getInstitutionsWithWardCount(): Flow<List<InstitutionWithWardCount>>
    
    /**
     * Gets institutions with their doctor count
     * @return Flow of institutions with doctor counts
     */
    fun getInstitutionsWithDoctorCount(): Flow<List<InstitutionWithDoctorCount>>
    
    /**
     * Gets total count of institutions
     * @return Total number of institutions
     */
    suspend fun getInstitutionCount(): Int
    
    /**
     * Updates an existing institution
     * @param institution Institution entity with updated information
     * @return Result indicating success or error
     */
    suspend fun updateInstitution(institution: Institution): Result<Unit>
    
    /**
     * Deletes an institution from the repository
     * @param institution Institution entity to delete
     * @return Result indicating success or error
     */
    suspend fun deleteInstitution(institution: Institution): Result<Unit>
    
    /**
     * Deletes an institution by ID
     * @param institutionId ID of institution to delete
     * @return Result indicating success or error
     */
    suspend fun deleteInstitutionById(institutionId: Int): Result<Unit>
    
    /**
     * Checks if an institution can be safely deleted
     * @param institutionId Institution ID to check
     * @return Result containing true if safe to delete, false otherwise
     */
    suspend fun canDeleteInstitution(institutionId: Int): Result<Boolean>
    
    // ========== WARD OPERATIONS ==========
    
    /**
     * Adds a new ward to an institution
     * @param ward Ward entity to add
     * @return Result containing the ward ID if successful, or error
     */
    suspend fun addWard(ward: Ward): Result<Long>
    
    /**
     * Adds multiple wards to institutions
     * @param wards List of wards to add
     * @return Result containing list of ward IDs if successful, or error
     */
    suspend fun addWards(wards: List<Ward>): Result<List<Long>>
    
    /**
     * Gets all wards for a specific institution
     * @param institutionId Institution ID to filter by
     * @return Flow of wards in the specified institution
     */
    fun getWardsByInstitution(institutionId: Int): Flow<List<Ward>>
    
    /**
     * Gets a ward by ID
     * @param wardId Ward ID to search for
     * @return Result containing ward if found, or error
     */
    suspend fun getWardById(wardId: Int): Result<Ward>
    
    /**
     * Searches wards by name
     * @param searchQuery Search term for ward name
     * @return Flow of matching wards
     */
    fun searchWardsByName(searchQuery: String): Flow<List<Ward>>
    
    /**
     * Gets wards that operate OPD on a specific day
     * @param day Day of the week
     * @return Flow of wards with OPD on the specified day
     */
    fun getWardsByOpdDay(day: String): Flow<List<Ward>>
    
    /**
     * Gets wards that operate OT on a specific day
     * @param day Day of the week
     * @return Flow of wards with OT on the specified day
     */
    fun getWardsByOtDay(day: String): Flow<List<Ward>>
    
    /**
     * Gets count of wards for a specific institution
     * @param institutionId Institution ID
     * @return Number of wards in the institution
     */
    suspend fun getWardCountByInstitution(institutionId: Int): Int
    
    /**
     * Updates an existing ward
     * @param ward Ward entity with updated information
     * @return Result indicating success or error
     */
    suspend fun updateWard(ward: Ward): Result<Unit>
    
    /**
     * Deletes a ward from the repository
     * @param ward Ward entity to delete
     * @return Result indicating success or error
     */
    suspend fun deleteWard(ward: Ward): Result<Unit>
    
    /**
     * Deletes a ward by ID
     * @param wardId ID of ward to delete
     * @return Result indicating success or error
     */
    suspend fun deleteWardById(wardId: Int): Result<Unit>
    
    /**
     * Checks if a ward can be safely deleted
     * @param wardId Ward ID to check
     * @return Result containing true if safe to delete, false otherwise
     */
    suspend fun canDeleteWard(wardId: Int): Result<Boolean>
    
    // ========== VALIDATION ==========
    
    /**
     * Validates institution data before saving
     * @param institution Institution entity to validate
     * @return Result indicating validation success or specific errors
     */
    suspend fun validateInstitution(institution: Institution): Result<Unit>
    
    /**
     * Validates ward data before saving
     * @param ward Ward entity to validate
     * @return Result indicating validation success or specific errors
     */
    suspend fun validateWard(ward: Ward): Result<Unit>
    
    // ========== COMPLEX OPERATIONS ==========
    
    /**
     * Creates an institution with its wards in a single transaction
     * @param institution Institution to create
     * @param wards List of wards to create for the institution
     * @return Result containing the institution ID if successful, or error
     */
    suspend fun createInstitutionWithWards(institution: Institution, wards: List<Ward>): Result<Long>
    
    /**
     * Updates institution ward count based on actual wards
     * @param institutionId Institution ID to update
     * @return Result indicating success or error
     */
    suspend fun updateInstitutionWardCount(institutionId: Int): Result<Unit>
}