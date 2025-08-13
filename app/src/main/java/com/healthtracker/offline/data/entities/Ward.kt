package com.healthtracker.offline.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.healthtracker.offline.data.converters.StringListConverter

/**
 * Room entity representing a Ward within an Institution.
 * 
 * @param wardId Primary key, auto-generated
 * @param institutionId Foreign key reference to Institution
 * @param wardName Name of the ward (required)
 * @param opdDays List of days when OPD (Outpatient Department) operates
 * @param otDays List of days when OT (Operation Theater) operates
 */
@Entity(
    tableName = "wards",
    foreignKeys = [
        ForeignKey(
            entity = Institution::class,
            parentColumns = ["institutionId"],
            childColumns = ["institutionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["institutionId"]),
        Index(value = ["wardName"])
    ]
)
@TypeConverters(StringListConverter::class)
data class Ward(
    @PrimaryKey(autoGenerate = true)
    val wardId: Int = 0,
    
    val institutionId: Int,
    
    val wardName: String,
    
    val opdDays: List<String> = emptyList(),
    
    val otDays: List<String> = emptyList()
) {
    /**
     * Validates the ward data
     * @return true if all required fields are valid
     */
    fun isValid(): Boolean {
        return wardName.isNotBlank() && 
               institutionId > 0 &&
               areValidDays(opdDays) &&
               areValidDays(otDays)
    }
    
    /**
     * Validates that all days in the list are valid day names
     */
    private fun areValidDays(days: List<String>): Boolean {
        val validDays = setOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", 
            "Friday", "Saturday", "Sunday"
        )
        return days.all { it in validDays }
    }
    
    /**
     * Returns formatted OPD days as a comma-separated string
     */
    fun getFormattedOpdDays(): String {
        return opdDays.joinToString(", ")
    }
    
    /**
     * Returns formatted OT days as a comma-separated string
     */
    fun getFormattedOtDays(): String {
        return otDays.joinToString(", ")
    }
    
    /**
     * Checks if the ward operates on a specific day for OPD
     */
    fun hasOpdOnDay(day: String): Boolean {
        return opdDays.contains(day)
    }
    
    /**
     * Checks if the ward operates on a specific day for OT
     */
    fun hasOtOnDay(day: String): Boolean {
        return otDays.contains(day)
    }
    
    companion object {
        /**
         * List of valid days of the week
         */
        val VALID_DAYS = listOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", 
            "Friday", "Saturday", "Sunday"
        )
    }
}