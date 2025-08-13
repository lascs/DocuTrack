package com.healthtracker.offline.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.healthtracker.offline.data.converters.StringListConverter

/**
 * Room entity representing a Doctor in the database.
 * 
 * @param doctorId Primary key, auto-generated
 * @param name Doctor's full name (required)
 * @param speciality Medical speciality (required)
 * @param pmdcNumber Pakistan Medical and Dental Council number (unique)
 * @param mobileNumber Contact mobile number
 * @param qualifications List of medical qualifications
 */
@Entity(
    tableName = "doctors",
    indices = [
        Index(value = ["pmdcNumber"], unique = true),
        Index(value = ["name"]),
        Index(value = ["speciality"])
    ]
)
@TypeConverters(StringListConverter::class)
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val doctorId: Int = 0,
    
    val name: String,
    
    val speciality: String,
    
    val pmdcNumber: String,
    
    val mobileNumber: String,
    
    val qualifications: List<String> = emptyList()
) {
    /**
     * Validates the doctor data
     * @return true if all required fields are valid
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               speciality.isNotBlank() && 
               pmdcNumber.isNotBlank() &&
               isValidMobileNumber(mobileNumber)
    }
    
    /**
     * Validates mobile number format
     * @param mobile Mobile number to validate
     * @return true if mobile number format is valid
     */
    private fun isValidMobileNumber(mobile: String): Boolean {
        // Basic validation for Pakistani mobile numbers
        val cleanNumber = mobile.replace(Regex("[^0-9]"), "")
        return cleanNumber.length >= 10 && cleanNumber.length <= 15
    }
    
    /**
     * Returns formatted qualifications as a comma-separated string
     */
    fun getFormattedQualifications(): String {
        return qualifications.joinToString(", ")
    }
}