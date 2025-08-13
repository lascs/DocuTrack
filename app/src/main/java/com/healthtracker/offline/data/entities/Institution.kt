package com.healthtracker.offline.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a Healthcare Institution in the database.
 * 
 * @param institutionId Primary key, auto-generated
 * @param name Institution name (required)
 * @param msName Medical Superintendent name
 * @param dmsName Deputy Medical Superintendent name
 * @param areaBrick Geographic area/brick identifier (required)
 * @param segmentName Healthcare segment classification
 * @param numberOfWards Total number of wards in the institution
 */
@Entity(
    tableName = "institutions",
    indices = [
        Index(value = ["name"]),
        Index(value = ["areaBrick"]),
        Index(value = ["segmentName"])
    ]
)
data class Institution(
    @PrimaryKey(autoGenerate = true)
    val institutionId: Int = 0,
    
    val name: String,
    
    val msName: String = "",
    
    val dmsName: String = "",
    
    val areaBrick: String,
    
    val segmentName: String = "",
    
    val numberOfWards: Int = 0
) {
    /**
     * Validates the institution data
     * @return true if all required fields are valid
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               areaBrick.isNotBlank() && 
               numberOfWards >= 0
    }
    
    /**
     * Returns a formatted display name for the institution
     */
    fun getDisplayName(): String {
        return if (areaBrick.isNotBlank()) {
            "$name ($areaBrick)"
        } else {
            name
        }
    }
    
    /**
     * Returns management information if available
     */
    fun getManagementInfo(): String {
        val parts = mutableListOf<String>()
        if (msName.isNotBlank()) parts.add("MS: $msName")
        if (dmsName.isNotBlank()) parts.add("DMS: $dmsName")
        return parts.joinToString(", ")
    }
}